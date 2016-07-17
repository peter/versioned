(ns content-api.model-includes.content-base-model
  (:require [content-api.model-includes.id-model :refer [id-spec]]
            [content-api.model-includes.typed-model :refer [typed-spec]]
            [content-api.model-includes.audited-model :refer [audited-spec]]
            [content-api.model-includes.versioned-model :refer [versioned-spec]]
            [content-api.model-includes.published-model :refer [published-spec]]
            [content-api.model-includes.validated-model :refer [validated-spec]]
            [content-api.model-includes.routed-model :refer [routed-spec]]))

(defn content-base-spec [type] [
  (id-spec)
  (typed-spec)
  (audited-spec)
  (versioned-spec :type type)
  (published-spec)
  (validated-spec)
  (routed-spec)
])
