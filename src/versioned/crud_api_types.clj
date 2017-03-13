(ns versioned.crud-api-types
  (:require [versioned.util.core :as u]
            [versioned.util.date :as d]
            [versioned.model-schema :refer [deep-child-schema attribute-type]]
            [schema.core :as s]
            [versioned.types :refer [Map Schema Function]]))

(s/defn safe-coerce :- (s/maybe s/Any)
  [coerce-fn :- Function
   schema :- (s/maybe Schema)
   attribute :- s/Keyword
   value :- s/Any]
  (try (coerce-fn schema attribute value)
    (catch Exception e
      ; TODO: this should be a debug log statement
      ; (println (str "type coercion failed for attribute=" attribute " value=" value " - " e))
      value)))

(s/defn coerce-map :- Map
  [coerce-fn :- Function attributes :- Map schema :- Schema]
  (reduce (fn [altered-map [k v]]
            (assoc altered-map k (safe-coerce coerce-fn schema k v)))
          {}
          attributes))

(s/defn coerce-value :- (s/maybe s/Any)
  [schema :- (s/maybe Schema) attribute :- s/Keyword value :- s/Any]
  (let [attribute-schema (deep-child-schema schema attribute)
        type (attribute-type attribute-schema attribute)]
    (cond
      (= (get-in attribute-schema [:x-meta :coerce]) false)
        value
      (u/blank? value)
        nil
      (= type "date")
        (d/parse-datetime value)
      (and (= type "boolean") (string? value))
        (u/parse-bool value)
      (and (= type "integer") (string? value))
        (u/parse-int value)
      (and (= type "array") (coll? value))
        (u/compact (map (partial coerce-value (:items attribute-schema) attribute) value))
      (and (= type "object") (:properties attribute-schema))
        (coerce-map coerce-value value attribute-schema)
      :else
        value)))

(s/defn coerce-attribute-types :- Map
  [schema :- Schema attributes :- Map]
  (coerce-map coerce-value attributes schema))
