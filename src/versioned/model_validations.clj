(ns versioned.model-validations
  (:require [clojure.string :as str]
            [versioned.util.core :as u]
            [versioned.model-attributes :refer [without-custom-keys]]
            [versioned.util.schema :refer [validate-schema]]))

(defn with-model-errors [doc errors]
  (let [new-meta (update-in (meta doc) [:errors] concat errors)]
    (with-meta doc new-meta)))

(defn model-errors [doc]
  (:errors (meta doc)))

(def model-not-updated [{:type "unchanged"}])

(defn validate-model-schema [schema doc]
  (validate-schema (without-custom-keys schema) doc))
