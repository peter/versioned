(ns versioned.controllers.sessions
  (:require [versioned.models.users :as users]
            [versioned.util.auth :as auth]
            [versioned.logger :as logger]
            [versioned.json-api :as json-api]
            [versioned.crud-api-attributes :refer [read-attributes]]))

(defn create [app request]
  (let [email (get-in request [:params :email])
        password (get-in request [:params :password])
        user (users/find-one app {:email email})
        model (get-in app [:models :users])]
      (if (users/authenticate user password)
        (let [access-token (auth/generate-token)
              updated (users/store-token app user access-token)
              doc (assoc (read-attributes model updated) :access_token access-token)]
          (logger/debug app "sessions create authenticated user:" updated (meta updated))
          (merge (json-api/doc-response model doc) {:headers (auth/header access-token)}))
        {:status 401})))
