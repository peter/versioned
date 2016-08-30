(ns versioned.swagger.parameters
  (:require [versioned.util.core :as u]
            [versioned.swagger.ref :refer [resolve-ref]]))

; Query parameters that can be passed multiple types are specified as type array.
; However, if you only pass such a parameter once the value will be a string.
; This function will wrap the string in an array.
(defn arrayify-attributes [schema attributes]
  (reduce (fn [result [attribute value]]
            (let [type (get-in schema [:properties attribute :type])
                  new-value (if (= type "array") (u/array value) value)]
              (assoc result attribute new-value)))
          {}
          attributes))

(defn parameters-in [path-spec in]
  (not-empty (->> (get-in path-spec [:parameters] {})
                  (filter #(= (% :in) in)))))

(defn property [parameter]
  (dissoc parameter :required :in :name :description))

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
