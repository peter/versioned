(ns versioned.controllers.home)

(defn index [app request]
  {:status 301
   :headers {"Location" "/swagger-ui/index.html"}})
