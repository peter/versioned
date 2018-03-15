(ns versioned.stubs
  (:require [versioned.model-init :refer [ref-models]]))

(def app {
  :config {:models {:articles "versioned.example.models.articles/spec"}}
  :models (ref-models {
    :articles {
      :type :articles
      :schema {
        :type "object"
      }
    }
  })
  :swagger {}
  :routes []
})
