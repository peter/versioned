(ns content-api.util.schema
  (:require [content-api.util.core :as u]
            [scjsv.core :as v]))

(defn json-type? [value]
  (boolean (some #(% value) [string? keyword? number? u/boolean? nil? map? vector?])))

; Test with: {:foo [(fn []) :foobar] :bar {:baz (fn []) :bla :bla}}
(defn schema-friendly-map [m]
  (u/deep-map-values #(if (json-type? %) % (.toString %)) m))

(defn validate-schema [schema doc]
  ((v/validator schema) (schema-friendly-map doc)))
