(ns content-api.components.db
  (:require [com.stuartsierra.component :as component]
            [content-api.db-api :refer [connect disconnect]]))

(defrecord Database [config]
  component/Lifecycle

  (start [component]
    (println "Starting Database")
    (let [uri (get-in config [:config :mongodb-url])
          {:keys [db conn]} (connect uri)]
      (assoc component :db db :conn conn)))

  (stop [component]
    (println "Stopping Database")
    (disconnect (:conn component))
    (dissoc component :db :conn)))

(defn new-database [& args]
  (map->Database (apply hash-map args)))
