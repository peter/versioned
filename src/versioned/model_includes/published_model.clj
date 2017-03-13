(ns versioned.model-includes.published-model
  (:require [versioned.model-includes.versioned-model :refer [latest-version]]))

(defn adjust-published-version
  "Make sure published version is not greater than latest version"
  [version published-version]
  (if (and published-version (> published-version version))
    version
    published-version))

(defn published-version-callback [doc options]
  (let [published-version (adjust-published-version (latest-version (:model-spec options) doc) (:published_version doc))]
    (if published-version
      (assoc doc :published_version published-version)
      doc)))

(def published-schema {
  :type "object"
  :properties {
    :published_version {:type "integer" :minimum 1 :x-meta {:versioned false}}
    :publish_at {:type "string" :format "date-time" :x-meta {:versioned false}}
    :unpublish_at {:type "string" :format "date-time" :x-meta {:versioned false}}
  }
})

(def published-callbacks {
  :save {
    :before [published-version-callback]
    :after []
  }
})

(defn published-spec [& options] {
  :schema published-schema
  :callbacks published-callbacks
})
