(ns versioned.controllers.import-sync
  (:require [clojure.set :refer [difference]]
            [versioned.util.core :as u]
            [versioned.json-api :refer [error-status]]
            [versioned.model-api :as model-api]
            [versioned.crud-api :as crud-api]
            [versioned.util.model :refer [get-model]]
            [versioned.model-support :refer [id-attribute]]
            [versioned.model-validations :refer [model-errors with-model-errors]]))

(defn- error-response? [response]
  (not (#{200 204} (:status response))))

(defn- insert-response? [response]
  (= :insert (:action response)))

(defn- update-response? [response]
  (and (= :update (:action response))
       (= 200 (:status response))))

(defn- write-response? [response]
  (= 200 (:status response)))

(defn- api-id [model doc]
  ((id-attribute model) doc))

(defn- delete-doc [api app user model id-field id]
  (let [query (hash-map id-field id)
        doc (first (model-api/find app model query))]
    (if doc
      (crud-api/delete api app {:params {:id (api-id model doc) :user user}})
      {:status 404 :body {:data {:query query}}})))

(defn- update-doc [api app user id doc]
  (println "import-sync/update-doc" id doc)
  (merge
    (crud-api/update api app {:params {:id id :data {:attributes doc}} :user user})
    {:action :update}))

(defn- insert-doc [api app user doc]
  (println "import-sync/insert-doc " doc)
  (merge
    (crud-api/create api app {:params {:data {:attributes doc}} :user user})
    {:action :insert}))

(defn- upsert-doc [api app user model id-field doc]
  (let [id (id-field doc)
        query (hash-map id-field id)
        existing-doc (first (model-api/find app model query))]
  (if existing-doc
    (update-doc api app user (api-id model existing-doc) doc)
    (insert-doc api app user doc))))

(defn delete [app request]
  (let [model-name (keyword (get-in request [:params :model]))
        model (get-model app model-name)
        user (:user request)
        api (crud-api/new-api :model-spec model)
        id-field (keyword (get-in request [:params :id_field]))
        new-ids (get-in request [:params :ids])
        existing-ids (map id-field (model-api/find app model {} {:per-page 100000 :sort (array-map id-field 1) :fields [id-field]}))
        delete-ids (difference (set existing-ids) (set new-ids))
        result (map (partial delete-doc api app user model id-field) delete-ids)
        errors (filter error-response? result)
        writes (filter write-response? result)
        status (if (empty? errors)
                 (if (empty? delete-ids)
                   204
                   200)
                  error-status)]
     (println "import-sync/delete" model-name "status:" status "deletes:" (count result) "errors:" (count errors) "new-ids:" (count new-ids) "existing-ids:" (count existing-ids))
     (if (not-empty errors) (clojure.pprint/pprint errors))
     {:body {:writes_count (count writes) :writes writes :errors errors} :status status}))

(defn upsert [app request]
  (let [model-name (keyword (get-in request [:params :model]))
        model (get-model app model-name)
        user (:user request)
        batch-index (get-in request [:params :batch_index] 0)
        api (crud-api/new-api :model-spec model)
        id-field (keyword (get-in request [:params :id_field]))
        data (get-in request [:params :data])
        result (map (partial upsert-doc api app user model id-field) data)
        errors (filter error-response? result)
        inserts (filter insert-response? result)
        updates (filter update-response? result)
        writes (filter write-response? result)
        status (if (empty? errors)
                 (if (and (empty? inserts) (empty? updates))
                   204
                   200)
                 error-status)]
     (println "import-sync/upsert" model-name "batch-index:"
                                   batch-index "status:" status
                                   "first-id:" (id-field (first data))
                                   "last-id:" (id-field (last data))
                                   "docs:" (count data)
                                   "inserts:" (count inserts)
                                   "updates:" (count updates)
                                   "errors:" (count errors))
     (if (not-empty errors) (clojure.pprint/pprint errors))
     {:body {:writes_count (count writes) :writes writes :errors errors} :status status}))
