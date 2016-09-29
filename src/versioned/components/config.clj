(ns versioned.components.config
  (:require [clojure.string :as str]
            [versioned.util.core :as u]
            [schema.core :as s]
            [com.stuartsierra.component :as component]))

(defn get-env [config]
  (or (:env config) (System/getenv "ENV") "development"))

(defn mongodb-url [env]
  (str "mongodb://127.0.0.1/versioned-" env))

(defn- default-config [env] {
  :require-read-auth true
  :session-expiry (* 60 60 24 14)
  :log-level (if (= "production" env) "info" "debug")
  :env env
  :validate-schemas (not= "production" env)
  :port 5000
  :mongodb-url (mongodb-url env)
  :start-web true
  :models {
    :users "versioned.models.users/spec"
    :changelog "versioned.models.changelog/spec"
  }
  :api-prefix "/v1"
})

(defn- env-key [config-key]
  (-> (name config-key)
      (str/upper-case)
      (str/replace "-" "_")))

(defn- env-value [config-key defaults]
  (let [value (System/getenv (env-key config-key))
        default-value (config-key defaults)]
    (cond
      (and value (integer? default-value)) (u/parse-int value)
      (and value (u/boolean? default-value)) (u/parse-bool value)
      :else value)))

(defn env-config [defaults]
  (u/compact (into {} (map #(vector % (env-value % defaults)) (keys defaults)))))

(defn- get-config [config]
  (let [defaults (default-config (get-env config))]
    (u/deep-merge defaults config (env-config defaults))))

; --------------------------------------------------------
; Component
; --------------------------------------------------------

(defrecord Config [config]
  component/Lifecycle

  (start [component]
    (let [config (get-config config)]
      (println "Starting Config" config)
      (when (:validate-schemas config)
        (s/set-fn-validation! true))
      (assoc component :config config)))

  (stop [component]
    (println "Stopping Config")
    (dissoc component :config)))

(defn new-config [& args]
  (map->Config {:config (apply hash-map args)}))
