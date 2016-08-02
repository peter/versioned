(ns versioned.middleware.auth
  (:require [versioned.util.auth :refer [parse-token]]
            [versioned.logger :as logger]
            [versioned.routes :refer [route-requires-auth?]]
            [clojure.string :as str]
            [versioned.models.users :as users]))

(def unauthorized-response {
  :status 401
  :body "Unauthorized"})

(def write-method? #{:post :put :patch})

(defn auth-required? [app request]
  (and (route-requires-auth? app request)
       (or (write-method? (:request-method request))
           (get-in app [:config :require-read-auth]))))

(defn require-auth [request handler app]
  (let [access-token (parse-token (:headers request))
        user-model (get-in app [:models :users])
        user (users/find-one app {:access_token access-token})]
    (if (and access-token user (not (users/token-expired? user (:config app))))
      (do (logger/debug app "versioned.middleware.auth/require-auth success user: " user)
          (handler (assoc request :user user)))
      (do (logger/debug app "versioned.middleware.auth/require-auth failure user: " user " token: " access-token)
          unauthorized-response))))

(defn wrap-auth [handler app]
  (fn [request]
    (if (auth-required? app request)
      (require-auth request handler app)
      (handler request))))
