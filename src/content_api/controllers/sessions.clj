(ns content-api.controllers.sessions
  (:require [content-api.models.users :as users]
            [content-api.util.auth :as auth]))

(defn create [app request]
  (let [email (get-in request [:params :email])
        password (get-in request [:params :password])
        user (users/find-one app {:email email})]
      (if (users/authenticate user password)
        (let [access-token (auth/generate-token)]
          (users/store-token app user access-token)
          {:status 200 :headers (auth/header access-token)})
        {:status 401})))
