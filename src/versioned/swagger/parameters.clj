(ns versioned.swagger.parameters
  (:require [versioned.util.core :as u]
            [versioned.swagger.ref :refer [resolve-ref]]))

(defn parameters-in [path-spec in]
  (not-empty (->> (get-in path-spec [:parameters] {})
                  (filter #(= (% :in) in)))))

(defn parameters-schema [parameters]
  (let [required (not-empty (->> (filter #(= (% :required) true) parameters)
                                 (map :name)))
        properties (reduce #(assoc %1
                                   (keyword (%2 :name))
                                   (dissoc %2 :required :in :name :description))
                           {}
                           parameters)]
    (u/compact {:type "object"
                :properties properties
                :additionalProperties false
                :required required})))
