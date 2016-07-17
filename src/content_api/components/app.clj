(ns content-api.components.app
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as str]
            [content-api.middleware.core :as middleware]
            [content-api.model-indexes :refer [ensure-indexes]]
            [content-api.model-init :refer [init-models]]
            [content-api.router.core :as router]
            [content-api.routes :refer [routes-with-handlers]]))

(defrecord Application [database config]
  component/Lifecycle

  (start [component]
    (let [config (:config config)
          models (init-models config)
          app {:config config :models models :database database}
          routes (routes-with-handlers app)
          handler (-> (router/create-handler app routes)
                      (middleware/wrap app))]
      (println "Starting Application config:" config "models:" (map :type (vals models)))
      (ensure-indexes database models)
      (assoc component :config config :models models :routes routes :handler handler)))

  (stop [component]
    (println "Stopping Application config:" config)
    (dissoc component :config :models :routes :handler)))

(defn new-application [& args]
  (map->Application (apply hash-map args)))
