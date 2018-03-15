(ns versioned.controllers.swagger)

(defn index [app request]
  {:body (deref (:swagger app)) :status 200})
