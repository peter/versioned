(ns content-api.model-attributes
  (:require [content-api.model-schema :refer [schema-attributes]]
            [clojure.string :as str]))

(def custom-property-keys #{:api_readable :api_writable :versioned :translated})

(defn api-writable? [attribute-schema]
  (get attribute-schema :api_writable true))

(defn api-writable-attribute-keys [schema]
  (let [schema-attrs (schema-attributes schema)]
    (filter #(api-writable? (% schema-attrs)) (keys schema-attrs))))

(defn api-writable-attributes [attributes schema]
  (select-keys attributes (api-writable-attribute-keys schema)))

(defn api-readable? [attribute-schema]
  (get attribute-schema :api_readable true))

(defn api-readable-attribute-keys [schema]
  (let [schema-attrs (schema-attributes schema)]
    (filter #(api-readable? (% schema-attrs)) (keys schema-attrs))))

(defn api-readable-attributes [attributes schema]
  (select-keys attributes (api-readable-attribute-keys schema)))

(defn translated-attribute
  ([locales custom-properties]
    (let [pattern (str "^" (str/join "|" locales) "$")
          default-properties {:type "string"}
          properties (merge default-properties custom-properties)]
      {
        :type "object"
        :translated true
        :patternProperties {
          pattern properties
        }
        :additionalProperties false
      }))
    ([locales]
      (translated-attribute locales {})))
