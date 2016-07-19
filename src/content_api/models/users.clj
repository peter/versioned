(ns content-api.models.users
  (:require [content-api.model-spec :refer [generate-spec]]
            [content-api.model-includes.id-model :refer [id-spec]]
            [content-api.model-api :as model-api]
            [content-api.util.core :as u]
            [content-api.util.encrypt :as encrypt]
            [content-api.util.date :as date]))

(defn spec [config]
  (generate-spec
    (id-spec)
    {
    :type :users
    :schema {
      :type "object"
      :properties {
        :name {:type "string"}
        :email {:type "string"}
        :password {:type "string" :api_readable false}
        :access_token {:type "string" :api_readable false}
        :access_token_created_at {:type "string" :format "date-time"}
      }
      :additionalProperties false
      :required [:name :email :password]
    }
    :indexes [
      {:fields [:email] :unique true}
      {:fields [:access_token] :unique true}
    ]
    :routes [:list :get]
}))

(defn authenticate [user password]
  (and user (encrypt/check password (:password user))))

(defn token-expired? [user config]
  (date/before? (:access_token_created_at user) (date/seconds-ago (:session-expiry config))))

(defn user-model [app]
  (get-in app [:models :users]))

; --------------------------------------------------------
; Database API
; --------------------------------------------------------

(defn find-one [app query]
  (first (model-api/find app (user-model app) query)))

(defn create [app attributes]
  (let [encrypted-password (encrypt/generate (:password attributes))
        secured-attributes (assoc attributes :password encrypted-password)]
    (model-api/create app (user-model app) secured-attributes)))

(defn store-token [app user access-token]
  (let [attributes (merge user {:access_token access-token
                                :access_token_created_at (date/now)})]
    (model-api/update app (user-model app) attributes)))
