(ns versioned.controllers.swagger)

(defn index [app request]
  {:body (:swagger app) :status 200})
