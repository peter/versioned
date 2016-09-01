(ns versioned.swagger.parameters
  (:require [versioned.util.core :as u]
            [versioned.swagger.ref :refer [resolve-ref]]))


; Convert parameter key like q[0] to just q
(defn array-attribute [attribute]
  (keyword (or (second (re-matches #"^(.+)(?:\[\d+\])$" (name attribute)))
               attribute)))

; Query parameters that can be passed multiple types are specified as type array.
; However, if you only pass such a parameter once the value will be a string.
; This function will wrap the string in an array.
; Also, many clients will pass array query params as q[0], q[1] etc. We collapse those
; to just q.
(defn arrayify-attributes [schema attributes]
  (reduce (fn [result [attribute value]]
            (let [array-attribute (array-attribute attribute)
                  array-value (concat (get result array-attribute []) (u/array value))
                  type (get-in schema [:properties array-attribute :type])]
                (if (= type "array")
                  (assoc result array-attribute array-value)
                  (assoc result attribute value))))
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
