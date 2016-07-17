(ns content-api.model-validations
  (:require [clojure.string :as str]
            [content-api.util.core :as u]
            [content-api.model-attributes :refer [custom-property-keys]]
            [content-api.util.schema :refer [validate-schema]]))

(defn with-model-errors [doc errors]
  (let [new-meta (update-in (meta doc) [:errors] concat errors)]
    (with-meta doc new-meta)))

(defn model-errors [doc]
  (:errors (meta doc)))

(def model-not-updated [{:type "unchanged"}])

(defn without-custom-keys
  "Drop custom property keys when validating to avoid validator warnings"
  [schema]
  (assoc schema :properties
                (u/map-values #(apply dissoc % custom-property-keys) (:properties schema))))

(defn validate-model-schema [schema doc]
  (validate-schema (without-custom-keys schema) doc))
