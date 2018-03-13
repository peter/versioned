(ns versioned.middleware.auth
  (:require [versioned.util.auth :refer [parse-token]]
            [versioned.logger :as logger]
            [clojure.string :as str]
            [versioned.models.users :as users]))

(defn route-requires-auth? [app request]
  (if (:route request)
      (get-in request [:route :swagger :x-auth-required] true)
      false))

(def unauthorized-response {
  :status 401
  :body "Unauthorized"})

(def write-method? #{:post :put :patch :delete})

(defn read-auth-required? [app request]
  (let [app-read-auth (get-in app [:config :require-read-auth])
        model (get-in request [:route :swagger :x-model])
        model-read-auth (get-in app [:models model :schema :x-meta :require-read-auth])]
    (if (nil? model-read-auth) app-read-auth model-read-auth)))

(defn write-auth-required? [request]
  (let [published? (get-in request [:query-params :published])
        version-token? (get-in request [:query-params :version_token])]
    (or (write-method? (:request-method request))
        (not (or published?
                 version-token?)))))

(defn auth-required? [app request]
  (and (route-requires-auth? app request)
       (or (read-auth-required? app request)
           (write-auth-required? request))))

(defn user-authorized? [user request]
  (or (not (write-auth-required? request))
      (= (:permission user) "write")))

(defn require-auth [request handler app]
  (let [access-token (parse-token (:headers request))
        user-model (get-in app [:models :users])
        user (users/find-one app {:access_token access-token})]
    (if (and access-token
             user
             (not (users/token-expired? user (:config app)))
             (user-authorized? user request))
      (do (logger/debug app "versioned.middleware.auth/require-auth success user:" user)
          (handler (assoc request :user user)))
      (do (logger/debug app "versioned.middleware.auth/require-auth failure user:" user " token: " access-token " write-auth-required: " (write-auth-required? request))
          unauthorized-response))))

(defn wrap-auth [handler app]
  (fn [request]
    (if (auth-required? app request)
      (require-auth request handler app)
      (handler request))))
