(ns versioned.model-attributes
  (:require [versioned.model-schema :refer [schema-attributes restricted-schema]]
            [versioned.util.core :as u]
            [clojure.string :as str]))

(def custom-property-keys #{:meta})

(defn without-custom-keys
  "Drop custom property keys when validating schema to avoid validator warnings or swagger errors"
  [schema]
  (assoc schema :properties
                (u/map-values #(apply dissoc % custom-property-keys) (:properties schema))))

(defn api-writable? [attribute-schema]
  (get-in attribute-schema [:meta :api_writable] true))

(defn api-writable-attribute-keys [schema]
  (let [schema-attrs (schema-attributes schema)]
    (filter #(api-writable? (% schema-attrs)) (keys schema-attrs))))

(defn api-writable-attributes [schema attributes]
  (select-keys attributes (api-writable-attribute-keys schema)))

(defn api-writable-schema [schema]
  (restricted-schema schema (api-writable-attribute-keys schema)))

(defn api-readable? [attribute-schema]
  (get-in attribute-schema [:meta :api_readable] true))

(defn api-readable-attribute-keys [schema]
  (let [schema-attrs (schema-attributes schema)]
    (filter #(api-readable? (% schema-attrs)) (keys schema-attrs))))

(defn api-readable-attributes [schema attributes]
  (select-keys attributes (api-readable-attribute-keys schema)))

(defn api-readable-schema [schema]
  (restricted-schema schema (api-readable-attribute-keys schema)))

(defn translated-attribute
  ([locales custom-schema]
    ; NOTE: could use JSON schema patternProperties with (str "^(" (str/join "|" locales) ")$") here,
    ;       but patternProperties is currently not allowed by Swagger.
    (let [default-attribute-schema {:type "string"}
          attribute-schema (merge default-attribute-schema custom-schema)
          properties (reduce #(assoc %1 %2 attribute-schema) {} locales)]
      {
        :type "object"
        :meta {
          :translated true
        }
        :properties properties
        :additionalProperties false
      }))
    ([locales]
      (translated-attribute locales {})))
