(ns versioned.controllers.sessions
  (:require [versioned.models.users :as users]
            [versioned.util.auth :as auth]
            [versioned.logger :as logger]))

(defn create [app request]
  (let [email (get-in request [:params :email])
        password (get-in request [:params :password])
        user (users/find-one app {:email email})]
      (if (users/authenticate user password)
        (let [access-token (auth/generate-token)
              updated (users/store-token app user access-token)]
          (logger/debug "sessions create authenticated user:" updated (meta updated))
          {:status 200 :headers (auth/header access-token)})
        {:status 401})))
