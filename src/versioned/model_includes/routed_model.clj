(ns versioned.model-includes.routed-model
  (:require [versioned.routes :refer [crud-actions]]))

(defn routed-spec [& options] {
  :routes crud-actions
})
