(ns versioned.model-attributes
  (:require [versioned.model-schema :refer [schema-attributes restricted-schema]]
            [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.types :refer [JsonSchema]]
            [clojure.string :as str]
            [clojure.set :refer [intersection]]))

(def custom-property-keys #{:meta})

(s/defn map-with-custom-keys? :- s/Bool
  [value :- s/Any]
  (boolean (and (map? value)
                (not-empty (intersection (set (keys value)) custom-property-keys)))))

(s/defn without-custom-keys :- JsonSchema
  "Drop custom property keys when validating schema to avoid validator warnings or swagger errors"
  [schema :- JsonSchema]
  (let [f #(if (map-with-custom-keys? %)
               (apply dissoc % custom-property-keys)
               %)]
    (clojure.walk/prewalk f schema)))

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
