(ns content-api.model-includes.versioned-model
  (:require [content-api.model-support :as model-support]
            [content-api.model-changes :refer [model-changes]]
            [content-api.model-versions :refer [versioned-attributes versioned-coll]]
            [content-api.util.db :as db-util]
            [content-api.util.date :as d]
            [content-api.db-api :as db]))

(defn increment-version? [model-spec doc]
  (not-empty (select-keys (model-changes model-spec doc)
                          (versioned-attributes (:schema model-spec)))))

(defn latest-version [model-spec doc]
  (let [old-version (get-in (meta doc) [:existing-doc :version])]
    (if old-version
      (if (increment-version? model-spec doc) (inc old-version) old-version)
      1)))

(defn versioned-doc [model-spec doc]
  (let [model-attributes (select-keys doc (versioned-attributes (:schema model-spec)))
        version-attributes {
          :created_at (d/now)
          :created_by (or (:updated_by doc) (:created_by doc))}]
    (merge model-attributes version-attributes)))

(defn set-version-callback [doc options]
  (assoc doc :version (latest-version (:model-spec options) doc)))

(defn create-version-callback [doc options]
  (println "create-version-callback" (increment-version? (:model-spec options) doc) (versioned-doc (:model-spec options) doc))
  (if (increment-version? (:model-spec options) doc)
    (let [result (db/create (:database options) (versioned-coll doc) (versioned-doc (:model-spec options) doc))]))
  doc)

(defn remove-version-callbacks [doc options]
  (let [id-attribute (model-support/id-attribute (:model-spec options))
        query (select-keys doc [id-attribute])
        result (db/delete (:database options) (versioned-coll doc) query)]
    doc))

(def versioned-schema {
  :type "object"
  :properties {
    :version {:type "integer" :minimum 1 :meta {:api_writable false}}
  }
  :required [:version]
})

(def versioned-callbacks {
  :save {
    :before [set-version-callback]
    :after [create-version-callback]
  }
  :delete {
    :after [remove-version-callbacks]
  }
})

(defn versioned-indexes [type] [
  {:fields [:id :version] :coll (versioned-coll {:type type}) :unique true}
])

(defn versioned-relationships [type id-attribute]
  {
    :versions {
      :from_coll (versioned-coll {:type type})
      :from_model type
      :from_field id-attribute
      :to_coll (model-support/coll {:type type})
      :to_model nil
      :to_field id-attribute
      :find_opts {
        :per-page 20
        :fields [:id :type :title :widgets_ids :version :published_version :created_at :created_by]
        :sort (array-map :version -1)
      }
  }})

(defn versioned-spec [& {:keys [type id-attribute] :or {id-attribute :id}}] {
  :schema versioned-schema
  :callbacks versioned-callbacks
  :indexes (versioned-indexes type)
  :relationships (versioned-relationships type id-attribute)
})
