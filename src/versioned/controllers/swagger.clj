(ns versioned.controllers.swagger
  (:require [versioned.swagger.core :refer [swagger]]))

(defn index [app request]
  {:body (swagger app) :status 200})
