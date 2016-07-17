(ns content-api.model-api
  (:refer-clojure :exclude [find update delete count])
  (:require [content-api.db-api :as db]
            [content-api.model-versions :refer [select-version]]
            [content-api.model-support :refer [coll id-attribute id-query valid-id?]]
            [content-api.model-versions :refer [unversioned-attributes versioned-coll
                                                  versioned-id-query apply-version]]
            [content-api.util.core :as u]
            [content-api.model-relationships :refer [with-relationships]]
            [content-api.model-validations :refer [with-model-errors model-not-updated]]
            [content-api.model-changes :refer [model-changed?]]
            [content-api.model-callbacks :refer [with-callbacks]]))

(defn find
  ([app model-spec query opts]
    (db/find (:database app) (coll model-spec) query opts))
  ([app model-spec query]
    (find app model-spec query {})))

(defn find-one
  ([app model-spec id opts]
    (if (valid-id? model-spec id)
      (let [doc (db/find-one (:database app) (coll model-spec) (id-query model-spec id))
            published? (:published opts)
            relationships-opts (select-keys opts [:published :relationships])
            version (select-version doc (:version opts) published?)]
        (if (and version (not= version (:version doc)))
          (let [versioned-doc (db/find-one (:database app) (versioned-coll model-spec) (versioned-id-query model-spec id version))
                doc (apply-version model-spec doc versioned-doc)]
                (with-relationships app model-spec doc relationships-opts))
          (if (and published? (not (:published_version doc)))
              nil
              (with-relationships app model-spec doc relationships-opts))))
      nil))
  ([app model-spec id]
    (find-one app model-spec id {})))

(defn count [app model-spec query]
  (db/count (:database app) (coll model-spec) query))

(defn- exec-create-without-callbacks [app model-spec doc]
  (let [result (db/create (:database app) (coll model-spec) doc)]
    (with-meta (:doc result)
               (merge (meta doc) {:result result}))))

(def exec-create (with-callbacks exec-create-without-callbacks :create))

(defn create [app model-spec doc]
  (let [create-doc (u/compact doc)]
    (exec-create app model-spec create-doc)))

(defn- exec-update-without-callbacks [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        result (db/update (:database app) (coll model-spec) (id-query model-spec id) doc)]
      (with-meta (:doc result)
                 (merge (meta doc) {:result result}))))

(def exec-update (with-callbacks exec-update-without-callbacks :update))

(defn update [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        existing-doc (find-one app model-spec id)
        merged-doc (with-meta (u/compact (merge existing-doc doc)) {:existing-doc existing-doc})]
    (if (model-changed? model-spec merged-doc)
      (exec-update app model-spec merged-doc)
      (with-model-errors doc model-not-updated))))

(defn- delete-without-callbacks [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        result (db/delete (:database app) (coll model-spec) (id-query model-spec id))]
    (with-meta doc {:result result})))

(def delete (with-callbacks delete-without-callbacks :delete))
