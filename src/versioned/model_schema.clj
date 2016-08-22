(ns versioned.model-schema
  (:require [versioned.util.core :as u]))

(defn schema-attributes [schema]
  (or (:properties schema)
      (and (:patternProperties schema) (first (vals (:patternProperties schema))))))

(defn child-schema [schema attribute]
  (cond
    (= (:type schema) "object") (get (schema-attributes schema) attribute)
    (= (:type schema) "array") (:items schema)
    :else schema))

(defn attribute-type [schema attribute]
  (cond
    (= (:format schema) "date-time") "date"
    :else (:type schema)))

(defn restricted-schema [schema allowed-properties]
  (let [properties (select-keys (:properties schema) allowed-properties)
        required (if (:required schema) (filter (set allowed-properties) (:required schema)))]
    (merge schema (u/compact {:properties properties :required required}))))
