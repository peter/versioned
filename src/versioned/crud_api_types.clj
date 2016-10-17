(ns versioned.crud-api-types
  (:require [versioned.util.core :as u]
            [versioned.util.date :as d]
            [versioned.model-schema :refer [deep-child-schema attribute-type]]
            [schema.core :as s]
            [versioned.types :refer [Map Schema Function]]
            [clojure.stacktrace]))

(s/defn coerce-map :- Map
  [coerce-fn :- Function attributes :- Map schema :- Schema]
  (reduce (fn [altered-map [k v]]
            (assoc altered-map k (coerce-fn schema k v)))
          {}
          attributes))

(s/defn safe-coerce-map :- Map
  [coerce-fn :- Function attributes :- Map schema :- Schema]
  (try (coerce-map coerce-fn attributes schema)
    (catch Exception e
      (println "coerce-map exception " (.getMessage e))
      (clojure.stacktrace/print-stack-trace e)
      (println "coerce-map (type attributes)=" (type attributes))
      (clojure.pprint/pprint attributes)
      (clojure.pprint/pprint schema)
      attributes)))

(s/defn coerce-value :- (s/maybe s/Any)
  [schema :- (s/maybe Schema) attribute :- s/Keyword value :- s/Any]
  (let [attribute-schema (deep-child-schema schema attribute)
        type (attribute-type attribute-schema attribute)]
    (cond
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
        (safe-coerce-map coerce-value value attribute-schema)
      :else
        value)))

(s/defn coerce-attribute-types :- Map
  [schema :- Schema attributes :- Map]
  (safe-coerce-map coerce-value attributes schema))
