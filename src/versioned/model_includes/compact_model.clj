(ns versioned.model-includes.compact-model
  (:require [versioned.util.core :as u]))

(defn compact-callback [doc options]
  (let [compact-property (fn [value]
                             (if (coll? value)
                              (not-empty (u/compact value))
                              value))]
    (u/compact (u/map-values compact-property doc))))

(def compact-callbacks {
  :save {
    :before [compact-callback]
  }
})

(defn compact-spec [& options] {
  :callbacks compact-callbacks
})
