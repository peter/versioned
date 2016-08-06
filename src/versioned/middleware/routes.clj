(ns versioned.middleware.routes
  (:require [versioned.router.match :refer [find-match]]))

; NOTE: we need to access the matching route for a request multiple times
; but we only want to generate it once
(defn wrap-route-match [handler app]
  (fn [request]
    (let [route-match (find-match (:routes app) request)
          route (:route route-match)
          path-params (:params route-match)]
      (handler (assoc request :route route
                              :path-params path-params
                              :params (merge (:params request) path-params))))))
