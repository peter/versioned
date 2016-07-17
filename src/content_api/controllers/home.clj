(ns content-api.controllers.home)

(defn index [app request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Welcome to Content API"})
