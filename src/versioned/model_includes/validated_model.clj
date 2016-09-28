(ns versioned.model-includes.validated-model
  (:require [versioned.model-validations :refer [validate-model-schema with-model-errors]]
            [versioned.logger :as logger]))

(defn validate-schema-callback [doc options]
  (if-let [errors (validate-model-schema (:schema options) doc)]
    (do
      (logger/debug (:app options) "validated-model/validate-schema-callback errors:" errors)
      (with-model-errors doc errors))
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
