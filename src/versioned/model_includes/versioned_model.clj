(ns versioned.model-includes.versioned-model
  (:require [versioned.model-support :as model-support]
            [versioned.model-changes :refer [model-changes]]
            [versioned.logger :as logger]
            [versioned.model-versions :refer [versioned-attributes versioned-coll]]
            [versioned.util.db :as db-util]
            [versioned.util.date :as d]
            [versioned.util.digest :as digest]
            [versioned.db-api :as db]))

(defn versioned-changes [model-spec doc]
  (select-keys (model-changes model-spec doc)
               (versioned-attributes (:schema model-spec))))

(defn increment-version? [model-spec doc]
  (let [old-version (get-in (meta doc) [:existing-doc :version])
        changes (versioned-changes model-spec doc)]
    (and (not-empty changes)
         (or (not old-version)
             (and (:published_version doc) (>= (:published_version doc) old-version))))))

(defn latest-version [model-spec doc]
  (let [old-version (get-in (meta doc) [:existing-doc :version])]
    (if old-version
      (if (increment-version? model-spec doc) (inc old-version) old-version)
      1)))

(defn version-token [doc options]
  (if (or (not (:version_token doc))
          (increment-version? (:model-spec options) doc))
      (digest/generate)
      (:version_token doc)))

(defn versioned-doc [model-spec doc]
  (let [model-attributes (select-keys doc (versioned-attributes (:schema model-spec)))
        version-attributes {
          :created_at (d/now)
          :created_by (or (:updated_by doc) (:created_by doc))}]
    (dissoc (merge model-attributes version-attributes) :_id)))

(defn set-version-callback [doc options]
  (let [new-version (latest-version (:model-spec options) doc)]
    (logger/debug (:app options) "versioned-model/set-version-callback"
                                 "old-version:" (get-in (meta doc) [:existing-doc :version])
                                 "new-version:" new-version
                                 "versioned-changes:" (versioned-changes (:model-spec options) doc))
    (assoc doc :version new-version :version_token (version-token doc options))))

(defn update-version-callback [doc options]
  (if (not (increment-version? (:model-spec options) doc))
    (let [id-attribute (model-support/id-attribute (:model-spec options))
          query (select-keys doc [id-attribute :version])
          updated-doc (versioned-doc (:model-spec options) doc)
          result (db/update (:database options) (versioned-coll doc) query updated-doc)]))
  doc)

(defn create-version-callback [doc options]
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
    :version {:type "integer" :minimum 1 :x-meta {:api_writable false}}
    :version_token {:type "string" :x-meta {:api_writable false}}
  }
  :required [:version :version_token]
})

(def versioned-callbacks {
  :save {
    :before [set-version-callback]
    :after [update-version-callback create-version-callback]
  }
  :delete {
    :after [remove-version-callbacks]
  }
})

(defn versioned-indexes [type] [
  {:fields [:id :version] :coll (versioned-coll {:type type}) :unique true}
  ; {:fields [:id :version_token] :coll (versioned-coll {:type type}) :unique true}
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
        :per-page 5
        :sort (array-map :version -1)
      }
  }})

(defn versioned-spec [& {:keys [type id-attribute] :or {id-attribute :id}}] {
  :schema versioned-schema
  :callbacks versioned-callbacks
  :indexes (versioned-indexes type)
  :relationships (versioned-relationships type id-attribute)
})
