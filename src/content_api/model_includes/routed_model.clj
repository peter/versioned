(ns content-api.model-includes.routed-model
  (:require [content-api.routes :refer [crud-actions]]))

(defn routed-spec [& options] {
  :routes crud-actions
})
