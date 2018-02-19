(ns versioned.model-api
  (:refer-clojure :exclude [find update delete count])
  (:require [versioned.db-api :as db]
            [versioned.logger :as logger]
            [schema.core :as s]
            [versioned.types :refer [Map App Model Doc ID PosInt Function]]
            [versioned.model-versions :refer [select-version]]
            [versioned.model-support :refer [coll id-attribute id-query valid-id?]]
            [versioned.model-versions :refer [unversioned-attributes versioned-coll
                                                  versioned-id-query apply-version]]
            [versioned.util.core :as u]
            [versioned.model-relationships :refer [with-relationships with-published-versions]]
            [versioned.model-validations :refer [with-model-errors
                                                 model-not-updated
                                                 duplicate-key-error]]
            [versioned.model-changes :refer [model-changes]]
            [versioned.model-callbacks :refer [with-callbacks]])
  (:import [com.mongodb DuplicateKeyException]))

(s/defn find :- [Doc]
  ([app :- App
    model :- Model
    query :- Map
    opts :- Map]
   (let [published-query (if (:published opts)
                             {:published_version {:$ne nil}}
                             nil)
         db-query (merge query published-query)
         docs (db/find (:database app) (coll model) db-query opts)]
      (with-published-versions app docs (:type model) opts)))
  ([app :- App
    model :- Model
    query :- Map]
   (find app model query {})))

(s/defn find-one :- (s/maybe Doc)
  ([app :- App
    model :- Model
    id :- ID
    opts :- Map]
   (if (valid-id? model id)
     (let [doc (db/find-one (:database app) (coll model) (id-query model id))
           published? (:published opts)
           relationships-opts (select-keys opts [:published :relationships])
           version (select-version doc (:version opts) published?)]
       (if (and version (not= version (:version doc)))
         (let [versioned-doc (db/find-one (:database app) (versioned-coll model) (versioned-id-query model id version))
               doc (apply-version model doc versioned-doc)]
              (with-relationships app model doc relationships-opts))
         (if (and published? (not (:published_version doc)))
             nil
             (with-relationships app model doc relationships-opts))))
     nil))
  ([app :- App
    model :- Model
    id :- ID]
   (find-one app model id {})))

(s/defn count :- PosInt
  [app :- App
   model :- Model
   query :- Map]
  (db/count (:database app) (coll model) query))

(s/defn exec-create-without-callbacks :- Doc
  [app :- App
   model :- Model
   doc :- Doc]
  (let [result (db/create (:database app) (coll model) doc)]
    (logger/debug app "model-api/create result:" result)
    (with-meta (:doc result)
               (assoc (meta doc) :result (:result result)))))

(s/defn with-db-error-handling :- Function
  [write-fn :- Function]
  (fn [app model doc]
    (try
      (write-fn app model doc)
      (catch DuplicateKeyException e
        (logger/debug app "model-api/with-db-error-handling exception: " (type e) (.getMessage e))
        (with-model-errors doc (duplicate-key-error (.getMessage e)))))))

(def exec-create
  (-> exec-create-without-callbacks
    (with-callbacks :create)
    (with-db-error-handling)))

(s/defn create :- Doc
  [app :- App
   model :- Model
   doc :- Doc]
  (let [create-doc (u/compact doc)]
    (exec-create app model create-doc)))

(s/defn exec-update-without-callbacks :- Doc
  [app :- App
   model :- Model
   doc :- Doc]
  (let [changes (model-changes model doc)]
    (logger/debug app "model-api/exec-update-without-callbacks" (:type doc) ((id-attribute model) doc) "changes:" changes)
    (if changes
      (let [id ((id-attribute model) doc)
            result (db/update (:database app) (coll model) (id-query model id) doc)]
          (logger/debug app "model-api/update result:" result)
          (with-meta (:doc result)
                     (merge (meta doc) {:result result})))
      (with-model-errors doc model-not-updated))))

(def exec-update
  (-> exec-update-without-callbacks
    (with-callbacks :update)
    (with-db-error-handling)))

(s/defn update :- Doc
  [app :- App
   model :- Model
   doc :- Doc]
  (let [id ((id-attribute model) doc)
        existing-doc (find-one app model id)
        merged-doc (with-meta (u/compact (merge existing-doc doc)) {:existing-doc existing-doc})]
    (exec-update app model merged-doc)))

(s/defn delete-without-callbacks :- Doc
  [app :- App
   model :- Model
   doc :- Doc]
  (let [id ((id-attribute model) doc)
        result (db/delete (:database app) (coll model) (id-query model id))]
    (with-meta doc {:result result})))

(def delete (with-callbacks delete-without-callbacks :delete))
