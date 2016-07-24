(ns versioned.controllers.home)

(defn index [app request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Welcome to the Versioned API"})
