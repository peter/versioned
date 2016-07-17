(ns content-api
  (:require [com.stuartsierra.component :as component]
            [content-api.components.config :refer [new-config]]
            [content-api.components.db :refer [new-database]]
            [content-api.components.app :refer [new-application]]
            [content-api.components.web :refer [new-webserver]]))

(defn new-system [& config]
  (component/system-map
    :config (apply new-config config)
    :database (component/using (new-database) [:config])
    :app (component/using (new-application) [:config :database])
    :web (component/using (new-webserver) [:app])))

(defn -main [& args]
  (component/start (apply new-system args)))
