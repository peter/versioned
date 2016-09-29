(ns versioned.logger
  (:require [clojure.string :as str]
            [schema.core :as s]
            [versioned.schema :refer [Nil App LogLevel]]))

(s/defn level :- LogLevel
  [app :- App]
  (get-in app [:config :log-level]))

(s/defn level-prefix :- String
  [level :- LogLevel]
  (str "[" (str/upper-case level) "]"))

(s/defn log :- Nil
  [level :- LogLevel
   args :- [s/Any]]
  (let [print-args (into [(level-prefix level)] args)]
    (apply println print-args)))

(s/defn debug :- Nil
  [app :- App
   & args :- [s/Any]]
  (if (= (level app) "debug")
    (log "debug" args)))

(s/defn info :- Nil
  [app :- App
   & args :- [s/Any]]
  (log "info" args))
