(ns versioned.model-attributes
  (:require [versioned.model-schema :refer [schema-attributes restricted-schema]]
            [versioned.util.core :as u]
            [clojure.string :as str]
            [clojure.set :refer [intersection]]))

(def custom-property-keys #{:meta})

(defn map-with-custom-keys? [value]
  (and (map? value)
       (not-empty (intersection (set (keys value)) custom-property-keys))))

; TODO/FIXME: this only works if custom property keys are in "leaf nodes" of the schema tree
(defn without-custom-keys
  "Drop custom property keys when validating schema to avoid validator warnings or swagger errors"
  [schema]
  (let [f #(if (map? (:value %))
             (apply dissoc (:value %) custom-property-keys)
             (:value %))
        recurse-if? #(and (coll? (:value %))
                          (not (map-with-custom-keys? (:value %))))
        opts {:recurse-if? recurse-if?}]
    (u/deep-map-values f schema opts)))

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
