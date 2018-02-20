(ns versioned.example.searchable-model
  (:require [versioned.example.search.algolia :as search]))

(defn save-callback [doc options]
  (search/save (:app options) doc)
  doc)

(defn delete-callback [doc options]
  (search/delete (:app options) doc)
  doc)

(def callbacks {
  :save {
    :after [save-callback]
  }
  :delete {
    :after [delete-callback]
  }
})

(defn searchable-model-spec [] {
  :schema {
    :x-meta {
      :searchable true
    }
  }
  :callbacks callbacks
})
