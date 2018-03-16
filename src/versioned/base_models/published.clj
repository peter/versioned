(ns versioned.base-models.published
  (:require [versioned.base-models.content :as base-model]
            [versioned.model-includes.versioned-model :refer [versioned-spec]]
            [versioned.model-includes.published-model :refer [published-spec]]))

(defn spec [type] [
  (base-model/spec type)
  (versioned-spec :type type)
  (published-spec)
  {
    :schema {
      :x-meta {
        :base_model "published"
      }
    }
  }
])
