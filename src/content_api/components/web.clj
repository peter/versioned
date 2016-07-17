(ns content-api.components.web
  (:require [ring.adapter.jetty :as jetty]
            [com.stuartsierra.component :as component]))

(defrecord Webserver [port app options]
  component/Lifecycle

  (start [component]
    (when (get-in app [:config :start-web])
      (println "Starting Webserver")
      (let [port (get-in app [:config :port])
            options (merge {:port port :join? false} options)
            server (jetty/run-jetty (:handler app) options)]
        (assoc component :server server :port port :options options))))

  (stop [component]
    (when (:server component)
      (println "Stopping Webserver")
      (.stop (:server component))
      (.join (:server component))
      (dissoc component :server))))

(defn new-webserver [& args]
  (map->Webserver (apply hash-map args)))
