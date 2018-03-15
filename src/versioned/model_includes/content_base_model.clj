(ns versioned.model-includes.content-base-model
  (:require [versioned.model-includes.id-model :refer [id-spec]]
            [versioned.model-includes.typed-model :refer [typed-spec]]
            [versioned.model-includes.audited-model :refer [audited-spec]]
            [versioned.model-includes.validated-model :refer [validated-spec]]
            [versioned.model-includes.routed-model :refer [routed-spec]]
            [versioned.model-includes.compact-model :refer [compact-spec]]))

(defn content-base-spec [type] [
  (compact-spec)
  (id-spec)
  (typed-spec)
  (audited-spec)
  (validated-spec)
  (routed-spec)
])
