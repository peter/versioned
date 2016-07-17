(ns content-api.model-schema)

(defn schema-attributes [schema]
  (or (:properties schema)
      (and (:patternProperties schema) (first (vals (:patternProperties schema))))))

(defn child-schema [schema attribute]
  (cond
    (= (:type schema) "object") (attribute (schema-attributes schema))
    (= (:type schema) "array") (:items schema)
    :else schema))

(defn attribute-type [schema attribute]
  (cond
    (= (:format schema) "date-time") "date"
    :else (:type schema)))
