(ns versioned.model-includes.routed-model
  (:require [versioned.schema :refer [crud-actions]]))

(defn routed-spec [& options] {
  :routes crud-actions
})
