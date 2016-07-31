(ns versioned.middleware.auth
  (:require [versioned.util.auth :refer [parse-token]]
            [versioned.logger :as logger]
            [clojure.string :as str]
            [versioned.models.users :as users]))

(def unauthorized-response {
  :status 401
  :body "Unauthorized"})

(def write-method? #{:post :put :patch})

(defn auth-required? [app request]
  (let [api-prefix (get-in app [:config :api-prefix])]
    (if (write-method? (:request-method request))
        (not= (str api-prefix "/login") (:uri request))
        (and (get-in app [:config :require-read-auth])
             (str/starts-with? (:uri request) api-prefix)
             (not= (str api-prefix "/swagger.json") (:uri request))))))

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
