(ns versioned.crud-api-types
  (:require [versioned.util.core :as u]
            [versioned.util.date :as d]
            [versioned.model-schema :refer [child-schema attribute-type]]
            [clojure.stacktrace]))

(defn- coerce-map [coerce-fn attributes schema]
  (reduce (fn [altered-map [k v]]
            (assoc altered-map k (coerce-fn schema k v)))
          {}
          attributes))

(defn- safe-coerce-map [coerce-fn attributes schema]
  (try (coerce-map coerce-fn attributes schema)
    (catch Exception e
      (println "coerce-map exception " (.getMessage e))
      (clojure.stacktrace/print-stack-trace e)
      (println "coerce-map (type attributes)=" (type attributes))
      (clojure.pprint/pprint attributes)
      attributes)))

(defn- coerce-value [schema attribute value]
  (let [attribute-schema (child-schema schema attribute)
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
      (= type "array")
        (u/compact (map (partial coerce-value attribute-schema attribute) value))
      (and (= type "object") (:properties attribute-schema))
        (safe-coerce-map coerce-value value attribute-schema)
      :else
        value)))

(defn coerce-attribute-types [schema attributes]
  (safe-coerce-map coerce-value attributes schema))
