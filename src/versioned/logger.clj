(ns versioned.logger
  (:require [clojure.string :as str]))

(defn level [app]
  (get-in app [:config :log-level]))

(defn level-prefix [level]
  (str "[" (str/upper-case level) "]"))

(defn log [level args]
  (let [print-args (into [(level-prefix level)] args)]
    (apply println print-args)))

(defn debug [app & args]
  (if (= (level app) "debug")
    (log "debug" args)))

(defn info [app & args]
  (log "info" args))
