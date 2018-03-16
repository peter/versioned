(ns versioned.util.schema
  (:require [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.types :refer [Schema]]
            [scjsv.core :as v]))

; NOTE: there is no official merge support for JSON schema AFAIK and we cannot use "allOf"
(s/defn merge-schemas :- Schema
  [& schemas :- [Schema]]
  (let [x-meta (apply merge (map :x-meta schemas))
        properties (apply merge (map :properties schemas))
        required (not-empty (apply concat (map :required schemas)))]
    (u/compact (assoc (apply u/deep-merge schemas)
                      :x-meta x-meta
                      :properties properties
                      :required required))))

(defn json-type? [value]
  (boolean (some #(% value) [string? keyword? number? u/boolean? nil? map? vector?])))

; Test with: {:foo [(fn []) :foobar] :bar {:baz (fn []) :bla :bla}}
(defn schema-friendly-map [m]
  (u/deep-map-values (fn [{:keys [value]}]
                       (if (json-type? value)
                         value
                         (.toString value)))
                     m))

(defn validate-schema [schema doc]
  (let [errors (some->> (schema-friendly-map doc)
                        ((v/validator schema))
                        (remove #(= (:level %) "warning")))]
    (if (not-empty errors)
      (map #(assoc % :type "schema") errors)
      nil)))
