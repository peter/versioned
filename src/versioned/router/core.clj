(ns versioned.router.core
  (:require [versioned.logger :as logger]
            [versioned.router.match :as m]))

(defn missing-handler [app request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body "Missing"})

(defn log-route-match [app request]
  (logger/debug app (str
    "route match "
    (:remote-addr request) " "
    (:request-method request) " " (:uri request)
    " path-params: " (pr-str (:path-params request))
    " query-params: " (pr-str (:query-params request))
  )))

(defn create-handler [app]
  (fn [request]
    (let [handler (if (:route request) (get-in request [:route :handler]) missing-handler)]
    (when (:route request) (log-route-match app request))
    (handler app request))))
