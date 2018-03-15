(ns versioned.model-includes.published-base-model
  (:require [versioned.model-includes.content-base-model :refer [content-base-spec]]
            [versioned.model-includes.versioned-model :refer [versioned-spec]]
            [versioned.model-includes.published-model :refer [published-spec]]))

(defn published-base-spec [type] [
  (content-base-spec type)
  (versioned-spec :type type)
  (published-spec)
])
