(ns versioned.swagger.parameters
  (:require [versioned.util.core :as u]))

(defn parameters-in [swagger-spec in]
  (filter #(= (% :in) in)
          (get-in swagger-spec [:parameters] {})))

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
