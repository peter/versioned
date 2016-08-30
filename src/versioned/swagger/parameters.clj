(ns versioned.swagger.parameters
  (:require [versioned.util.core :as u]
            [versioned.swagger.ref :refer [resolve-ref]]))

(defn parameters-in [path-spec in]
  (not-empty (->> (get-in path-spec [:parameters] {})
                  (filter #(= (% :in) in)))))

(defn property [parameter]
  (let [type (if (= (:type parameter) "array")
                 ["array" "string"]
                 (:type parameter))]
    (merge (dissoc parameter :required :in :name :description)
           {:type type})))

(defn parameters-schema [parameters]
  (let [required (not-empty (->> (filter #(= (% :required) true) parameters)
                                 (map :name)))
        properties (reduce #(assoc %1
                                   (keyword (%2 :name))
                                   (property %2))
                           {}
                           parameters)]
    (u/compact {:type "object"
                :properties properties
                :additionalProperties false
                :required required})))
