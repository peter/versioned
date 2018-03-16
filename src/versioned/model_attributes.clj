(ns versioned.model-attributes
  (:require [versioned.model-schema :refer [schema-attributes restricted-schema]]
            [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.types :refer [Schema Attributes AttributeSet]]
            [clojure.string :as str]
            [clojure.set :refer [intersection]]))

; NOTE: we now use the 'x-' prefix for custom keys and they don't need to be removed
(def custom-property-keys #{})

(s/defn map-with-custom-keys? :- s/Bool
  [value :- s/Any]
  (boolean (and (map? value)
                (not-empty (intersection (set (keys value)) custom-property-keys)))))

(s/defn without-custom-keys :- Schema
  "Drop custom property keys when validating schema to avoid validator warnings or swagger errors"
  [schema :- Schema]
  (let [f #(if (map-with-custom-keys? %)
               (apply dissoc % custom-property-keys)
               %)]
    (clojure.walk/prewalk f schema)))

(s/defn api-writable? :- s/Bool
  [attribute-schema :- Schema]
  (get-in attribute-schema [:x-meta :api_writable] true))

(s/defn api-writable-attribute-keys :- AttributeSet
  [schema :- Schema]
  (let [schema-attrs (schema-attributes schema)]
    (set (filter #(api-writable? (% schema-attrs)) (keys schema-attrs)))))

(s/defn api-writable-attributes :- Attributes
  [schema :- Schema
   attributes :- Attributes]
  (select-keys attributes (api-writable-attribute-keys schema)))

(s/defn api-writable-schema :- Schema
  [schema :- Schema]
  (restricted-schema schema (api-writable-attribute-keys schema)))

(s/defn api-update? :- s/Bool
  [attribute-schema :- Schema]
  (get-in attribute-schema [:x-meta :api_update] true))

(s/defn api-update-attribute-keys :- AttributeSet
  [schema :- Schema]
  (let [schema-attrs (schema-attributes schema)]
    (set (filter #(api-update? (% schema-attrs)) (keys schema-attrs)))))

(s/defn api-update-attributes :- Attributes
  [schema :- Schema
   attributes :- Attributes]
  (select-keys (api-writable-attributes schema attributes) (api-update-attribute-keys schema)))

(s/defn api-readable? :- s/Bool
  [attribute-schema :- Schema]
  (get-in attribute-schema [:x-meta :api_readable] true))

(s/defn api-readable-attribute-keys :- AttributeSet
  [schema :- Schema]
  (let [schema-attrs (schema-attributes schema)]
    (set (filter #(api-readable? (% schema-attrs)) (keys schema-attrs)))))

(s/defn api-readable-attributes :- Attributes
  [schema :- Schema
   attributes :- Attributes]
  (select-keys attributes (api-readable-attribute-keys schema)))

(s/defn api-readable-schema :- Schema
  [schema :- Schema]
  (restricted-schema schema (api-readable-attribute-keys schema)))
