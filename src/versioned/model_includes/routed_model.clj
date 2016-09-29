(ns versioned.model-includes.routed-model
  (:require [versioned.types :refer [crud-actions]]))

(defn routed-spec [& options] {
  :routes crud-actions
})
