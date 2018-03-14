(ns versioned.model-validations
  (:require [clojure.string :as str]
            [versioned.util.core :as u]
            [versioned.model-attributes :refer [without-custom-keys]]
            [versioned.util.schema :refer [validate-schema]]))

(defn with-model-errors [doc errors]
  (if (u/present? errors)
    (let [new-meta (update-in (meta doc) [:errors] concat errors)]
      (with-meta doc new-meta))
    doc))

(defn model-errors [doc]
  (:errors (meta doc)))

(def model-not-updated [{:type "unchanged"}])

; Violation of mongodb unique index. Example message:
; "Write failed with error code 11000 and error message
; 'E11000 duplicate key error index: versioned-example.pages.$title.se_1 dup key: { : \"Startsida\" }'"
(defn duplicate-key-error [message]
  [{:type "duplicate_key" :message message}])

(defn validate-model-schema [schema doc]
  (validate-schema (without-custom-keys schema) doc))
