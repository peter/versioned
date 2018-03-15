(ns versioned.components.app
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as str]
            [versioned.swagger.core :refer [init-swagger]]
            [versioned.middleware.core :as middleware]
            [versioned.model-indexes :refer [ensure-indexes]]
            [versioned.model-init :refer [init-models]]
            [versioned.router.core :as router]
            [versioned.routes :refer [init-routes]]))

(defrecord Application [database config]
  component/Lifecycle

  (start [component]
    (let [config (:config config)
          models (atom (init-models config database))
          swagger (atom (init-swagger {:config config :models models}))
          app {:config config :models models :swagger swagger :database database}
          routes (atom (init-routes app))
          app (assoc app :routes routes)
          handler (-> (router/create-handler app)
                      (middleware/wrap app))]
      (println "Starting Application config:" config "models:" (map :type (vals (deref models))))
      (ensure-indexes app)
      (assoc component :config config
                       :models models
                       :swagger swagger
                       :routes routes
                       :handler handler)))

  (stop [component]
    (println "Stopping Application config:" config)
    (dissoc component :config :models :swagger :routes :handler)))

(defn new-application [& args]
  (map->Application (apply hash-map args)))
