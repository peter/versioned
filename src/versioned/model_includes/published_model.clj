(ns versioned.model-includes.published-model
  (:require [versioned.model-includes.versioned-model :refer [latest-version]]
            [versioned.model-changes :refer [model-changes]]
            [versioned.util.date :as d]
            [versioned.util.core :as u]))

(defn adjust-published-version
  "Make sure published version is not greater than latest version"
  [version published-version]
  (if (and published-version (> published-version version))
    version
    published-version))

(defn published-version-callback [doc options]
  (let [published-version (adjust-published-version (latest-version (:model-spec options) doc) (:published_version doc))]
    (if published-version
      (merge doc {:published_version published-version})
      doc)))

(defn published-audit-callback [doc options]
  (let [changes (model-changes (:model-spec options) doc)]
    (if (:published_version changes)
      (let [current-time (d/now)]
        (merge doc (u/compact {
          :first_published_at (or (:first_published_at doc) current-time)
          :last_published_at (and (:published_version doc) current-time)
        })))
      doc)))

(def published-schema {
  :type "object"
  :properties {
    :published_version {:type "integer" :minimum 1 :x-meta {:versioned false}}
    :first_published_at {:type "string" :format "date-time" :x-meta {:api_writable false :versioned false}}
    :last_published_at {:type "string" :format "date-time" :x-meta {:api_writable false :versioned false}}
    :publish_at {:type "string" :format "date-time" :x-meta {:versioned false}}
    :unpublish_at {:type "string" :format "date-time" :x-meta {:versioned false}}
  }
})

(def published-callbacks {
  :save {
    :before [published-version-callback published-audit-callback]
    :after []
  }
})

(defn published-spec [& options] {
  :schema published-schema
  :callbacks published-callbacks
})
