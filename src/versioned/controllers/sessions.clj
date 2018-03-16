(ns versioned.controllers.sessions
  (:require [versioned.models.users :as users]
            [versioned.util.auth :as auth]
            [versioned.logger :as logger]
            [versioned.json-api :as json-api]
            [versioned.util.model :refer [get-model]]
            [versioned.crud-api-attributes :refer [read-attributes]]))

(defn create [app request]
  (let [email (get-in request [:params :email])
        password (get-in request [:params :password])
        user (users/find-one app {:email email})
        model (get-model app :users)]
      (if (users/authenticate user password)
        (let [updated-user (if (or (nil? (:access_token user)) (users/token-expired? user (:config app)))
                           (users/store-token app user (auth/generate-token))
                           user)
              doc (assoc (read-attributes model updated-user) :access_token (:access_token updated-user))]
          (logger/debug app "sessions create authenticated user:" doc (meta doc))
          (merge (json-api/doc-response model doc) {:headers (auth/header (:access_token doc))}))
        {:status 401})))
