(ns versioned.middleware.routes
  (:require [versioned.router.core :refer [get-route-match]]))

; NOTE: we need to access the matching route for a request multiple times
; but we only want to generate it once
(defn wrap-route-match [handler app]
  (fn [request]
    (let [route-match (get-route-match app request)]
      (handler (assoc request :route-match route-match)))))
