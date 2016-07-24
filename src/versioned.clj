(ns versioned
  (:require [com.stuartsierra.component :as component]
            [versioned.components.config :refer [new-config]]
            [versioned.components.db :refer [new-database]]
            [versioned.components.app :refer [new-application]]
            [versioned.components.web :refer [new-webserver]]))

(defn new-system [& config]
  (component/system-map
    :config (apply new-config config)
    :database (component/using (new-database) [:config])
    :app (component/using (new-application) [:config :database])
    :web (component/using (new-webserver) [:app])))

(defn -main [& args]
  (component/start (apply new-system args)))
