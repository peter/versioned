(ns versioned.model-attributes
  (:require [versioned.model-schema :refer [schema-attributes]]
            [clojure.string :as str]))

(def custom-property-keys #{:meta})

(defn api-writable? [attribute-schema]
  (get-in attribute-schema [:meta :api_writable] true))

(defn api-writable-attribute-keys [schema]
  (let [schema-attrs (schema-attributes schema)]
    (filter #(api-writable? (% schema-attrs)) (keys schema-attrs))))

(defn api-writable-attributes [schema attributes]
  (select-keys attributes (api-writable-attribute-keys schema)))

(defn api-readable? [attribute-schema]
  (get-in attribute-schema [:meta :api_readable] true))

(defn api-readable-attribute-keys [schema]
  (let [schema-attrs (schema-attributes schema)]
    (filter #(api-readable? (% schema-attrs)) (keys schema-attrs))))

(defn api-readable-attributes [schema attributes]
  (select-keys attributes (api-readable-attribute-keys schema)))

(defn translated-attribute
  ([locales custom-properties]
    (let [pattern (str "^(" (str/join "|" locales) ")$")
          default-properties {:type "string"}
          properties (merge default-properties custom-properties)]
      {
        :type "object"
        :meta {
          :translated true
        }
        :patternProperties {
          pattern properties
        }
        :additionalProperties false
      }))
    ([locales]
      (translated-attribute locales {})))
