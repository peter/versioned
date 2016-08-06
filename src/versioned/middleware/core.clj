(ns versioned.middleware.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.reload :refer [wrap-reload]]
            [versioned.middleware.cors :refer [wrap-cors]]
            [versioned.middleware.auth :refer [wrap-auth]]
            [versioned.middleware.routes :refer [wrap-route-match]]
            [versioned.middleware.params-parser :refer [wrap-params-parser]]))

(defn development-middleware [handler env]
  (if (= env "development")
    (-> handler
      (wrap-reload))
    handler))

; NOTE: middleware execute execute in reverse order - the last one listed here exeucutes first
(defn wrap [handler app]
  (-> handler
      (wrap-params-parser app)
      (wrap-keyword-params)
      (wrap-params {})
      (wrap-json-params {})
      (wrap-json-response {:pretty true})
      (wrap-auth app)
      (wrap-cors)
      (development-middleware (:env app))
      (wrap-route-match app)
      (wrap-resource "public")
  ))
