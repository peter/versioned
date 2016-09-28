(ns versioned.model-api
  (:refer-clojure :exclude [find update delete count])
  (:require [versioned.db-api :as db]
            [versioned.logger :as logger]
            [versioned.model-versions :refer [select-version]]
            [versioned.model-support :refer [coll id-attribute id-query valid-id?]]
            [versioned.model-versions :refer [unversioned-attributes versioned-coll
                                                  versioned-id-query apply-version]]
            [versioned.util.core :as u]
            [versioned.model-relationships :refer [with-relationships]]
            [versioned.model-validations :refer [with-model-errors
                                                 model-not-updated
                                                 duplicate-key-error]]
            [versioned.model-changes :refer [model-changes]]
            [versioned.model-callbacks :refer [with-callbacks]])
   (:import [com.mongodb DuplicateKeyException]))

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
    (logger/debug "model-api/create result:" result)
    (with-meta (:doc result)
               (merge (meta doc) {:result result}))))

(defn with-db-error-handling [write-fn]
  (fn [app model-spec doc]
    (try
      (write-fn app model-spec doc)
      (catch DuplicateKeyException e
        (logger/debug "model-api/with-db-error-handling exception: " (type e) (.getMessage e))
        (with-model-errors doc (duplicate-key-error (.getMessage e)))))))

(def exec-create
  (-> exec-create-without-callbacks
    (with-callbacks :create)
    (with-db-error-handling)))

(defn create [app model-spec doc]
  (let [create-doc (u/compact doc)]
    (exec-create app model-spec create-doc)))

(defn- exec-update-without-callbacks [app model-spec doc]
  (let [changes (model-changes model-spec doc)]
    (logger/debug app "model-api/exec-update-without-callbacks" (:type doc) ((id-attribute model-spec) doc) "changes:" changes)
    (if changes
      (let [id ((id-attribute model-spec) doc)
            result (db/update (:database app) (coll model-spec) (id-query model-spec id) doc)]
          (logger/debug "model-api/update result:" result)
          (with-meta (:doc result)
                     (merge (meta doc) {:result result})))
      (with-model-errors doc model-not-updated))))

(def exec-update
  (-> exec-update-without-callbacks
    (with-callbacks :update)
    (with-db-error-handling)))

(defn update [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        existing-doc (find-one app model-spec id)
        merged-doc (with-meta (u/compact (merge existing-doc doc)) {:existing-doc existing-doc})]
    (exec-update app model-spec merged-doc)))

(defn- delete-without-callbacks [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        result (db/delete (:database app) (coll model-spec) (id-query model-spec id))]
    (with-meta doc {:result result})))

(def delete (with-callbacks delete-without-callbacks :delete))
