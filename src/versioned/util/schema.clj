(ns versioned.util.schema
  (:require [versioned.util.core :as u]
            [scjsv.core :as v]))

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
