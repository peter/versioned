(ns content-api.model-includes.validated-model
  (:require [content-api.model-validations :refer [validate-model-schema with-model-errors]]))

(defn validate-schema-callback [doc options]
  (if-let [errors (validate-model-schema (:schema options) doc)]
    (with-model-errors doc errors)
    doc))

; NOTE: we usually want validation to happen last of the before callbacks.
(def validated-callbacks {
  :save {
    :before [{:sort :last :fn validate-schema-callback}]
  }
})

(defn validated-spec [& options] {
  :callbacks validated-callbacks
})
