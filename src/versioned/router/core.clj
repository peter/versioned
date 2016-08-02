(ns versioned.router.core
  (:require [versioned.logger :as logger]
            [versioned.router.match :as m]))

(defn missing-handler [app request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body "Missing"})

(defn log-route-match [app request match]
  (logger/debug app (str
    "route match "
    (:remote-addr request) " "
    (:request-method request) " " (:uri request) " "
    (pr-str (:route match)) " "
    (pr-str (:params request)) " "
  )))

(defn get-route-match [app request]
  (or (:route-match request)
      (m/find-match (:routes app) request)))

(defn create-handler [app]
  (fn [request]
    (let [match (get-route-match app request)
          handler (if match (get-in match [:route :handler]) missing-handler)
          request-with-params (update-in request [:params] merge (:params match))]
    (when match (log-route-match app request-with-params match))
    (handler app request-with-params))))
