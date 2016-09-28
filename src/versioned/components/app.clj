(ns versioned.components.app
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [clojure.string :as str]
            [versioned.swagger.core :refer [swagger]]
            [versioned.middleware.core :as middleware]
            [versioned.model-indexes :refer [ensure-indexes]]
            [versioned.model-init :refer [init-models]]
            [versioned.router.core :as router]
            [versioned.routes :refer [routes-with-handlers]]))

(defrecord Application [database config]
  component/Lifecycle

  (start [component]
    (when (get-in config [:config :validate-schemas])
      (s/set-fn-validation! true))
    (let [config (:config config)
          models (init-models config)
          swagger-spec (swagger {:config config :models models})
          app {:config config :models models :swagger swagger-spec :database database}
          routes (routes-with-handlers app)
          app (assoc app :routes routes)
          handler (-> (router/create-handler app)
                      (middleware/wrap app))]
      (println "Starting Application config:" config "models:" (map :type (vals models)))
      (ensure-indexes app)
      (assoc component :config config
                       :models models
                       :swagger swagger-spec
                       :routes routes
                       :handler handler)))

  (stop [component]
    (println "Stopping Application config:" config)
    (dissoc component :config :models :swagger :routes :handler)))

(defn new-application [& args]
  (map->Application (apply hash-map args)))
