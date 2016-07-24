(ns versioned.model-includes.content-base-model
  (:require [versioned.model-includes.id-model :refer [id-spec]]
            [versioned.model-includes.typed-model :refer [typed-spec]]
            [versioned.model-includes.audited-model :refer [audited-spec]]
            [versioned.model-includes.versioned-model :refer [versioned-spec]]
            [versioned.model-includes.published-model :refer [published-spec]]
            [versioned.model-includes.validated-model :refer [validated-spec]]
            [versioned.model-includes.routed-model :refer [routed-spec]]))

(defn content-base-spec [type] [
  (id-spec)
  (typed-spec)
  (audited-spec)
  (versioned-spec :type type)
  (published-spec)
  (validated-spec)
  (routed-spec)
])
