(ns versioned.model-schema
  (:require [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.types :refer [Schema
                                     Nil
                                     AttributeType
                                     SchemaProperties
                                     Attribute
                                     AttributeSet]]
            [clojure.string :as str]))

(s/defn schema-attributes :- (s/maybe SchemaProperties)
  [schema :- (s/maybe Schema)]
  (or (:properties schema)
      (and (:patternProperties schema) (first (vals (:patternProperties schema))))))

(s/defn child-schema :- (s/maybe Schema)
 [schema :- (s/maybe Schema)
  attribute :- Attribute]
  (cond
    (= (:type schema) "object") (get (schema-attributes schema) attribute)
    (= (:type schema) "array") (:items schema)
    :else schema))

(s/defn attribute-path :- [s/Keyword]
  [attribute :- s/Keyword]
  (map keyword (str/split (name attribute) (u/str-to-regex "."))))

(s/defn deep-child-schema :- (s/maybe Schema)
  [schema :- (s/maybe Schema)
   attribute :- s/Keyword]
  (reduce child-schema schema (attribute-path attribute)))

(s/defn attribute-type :- (s/cond-pre AttributeType [AttributeType] Nil)
  [schema :- (s/maybe Schema)
   attribute :- s/Keyword]
  (cond
    (= (:format schema) "date-time") "date"
    :else (:type schema)))

(s/defn restricted-schema :- Schema
  [schema :- Schema
   allowed-properties :- AttributeSet]
  (let [properties (select-keys (:properties schema) allowed-properties)
        required (if (:required schema) (filter (set allowed-properties) (:required schema)))]
    (merge schema (u/compact {:properties properties :required required}))))
