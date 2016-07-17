(ns content-api.controllers.bulk-import
  (:require [content-api.db-api :as db]
            [content-api.util.core :as u]
            [content-api.model-api :as model-api]
            [content-api.model-support :refer [coll]]
            [content-api.model-versions :refer [versioned-coll]]
            [content-api.crud-api-attributes :refer [create-attributes]]
            [content-api.model-validations :refer [model-errors]]))

(defn- clear [app model-spec]
  (db/delete (:database app) (coll model-spec) {})
  (db/delete (:database app) (versioned-coll model-spec) {}))

(defn- insert-one [app model-spec request doc]
  (let [attributes (create-attributes model-spec request doc)]
    (model-api/create app model-spec attributes)))

(defn- insert [app model-spec request data]
  (map (partial insert-one app model-spec request) data))

(defn- one-result [doc]
  (u/compact {:errors (model-errors doc) :doc doc}))

(defn create [app request]
  (let [model-name (keyword (get-in request [:params :model]))
        model-spec (get-in app [:models model-name])
        data (get-in request [:params :data])
        _ (clear app model-spec)
        result (map one-result (insert app model-spec request data))]
     {:body {:result result} :status 200}))
