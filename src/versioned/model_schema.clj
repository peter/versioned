(ns versioned.model-schema
  (:require [versioned.util.core :as u]
            [clojure.string :as str]))

(defn schema-attributes [schema]
  (or (:properties schema)
      (and (:patternProperties schema) (first (vals (:patternProperties schema))))))

(defn child-schema [schema attribute]
  (cond
    (= (:type schema) "object") (get (schema-attributes schema) attribute)
    (= (:type schema) "array") (:items schema)
    :else schema))

(defn attribute-path [attribute]
  (map keyword (str/split (name attribute) (u/str-to-regex "."))))

(defn deep-child-schema [schema attribute]
  (reduce child-schema schema (attribute-path attribute)))

(defn attribute-type [schema attribute]
  (cond
    (= (:format schema) "date-time") "date"
    :else (:type schema)))

(defn restricted-schema [schema allowed-properties]
  (let [properties (select-keys (:properties schema) allowed-properties)
        required (if (:required schema) (filter (set allowed-properties) (:required schema)))]
    (merge schema (u/compact {:properties properties :required required}))))
