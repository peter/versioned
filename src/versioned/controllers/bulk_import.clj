(ns versioned.controllers.bulk-import
  (:require [versioned.db-api :as db]
            [versioned.util.core :as u]
            [versioned.json-api :refer [error-status]]
            [versioned.model-api :as model-api]
            [versioned.model-support :refer [coll]]
            [versioned.model-versions :refer [versioned-coll]]
            [versioned.crud-api-attributes :refer [create-attributes]]
            [versioned.model-validations :refer [model-errors with-model-errors]]
            [clojure.stacktrace]))

(defn- clear [app model-spec]
  (db/delete (:database app) (coll model-spec) {})
  (db/delete (:database app) (versioned-coll model-spec) {}))

(defn- insert-one [app model-spec request doc]
  (let [attributes (create-attributes model-spec request doc)]
    (try (model-api/create app model-spec attributes)
      (catch Exception e
        (println "bulk-import/insert-one exception " (.getMessage e))
        (clojure.stacktrace/print-stack-trace e)
        (println "bulk-import/insert-one attributes:")
        (clojure.pprint/pprint attributes)
        (with-model-errors attributes [{:type "db" :message (.getMessage e)}])))))

(defn- insert [app model-spec request data]
  (map (partial insert-one app model-spec request) data))

(defn- one-result [doc]
  (u/compact {:errors (model-errors doc) :doc doc}))

(defn create [app request]
  (let [model-name (keyword (get-in request [:params :model]))
        model-spec (get-in app [:models model-name])
        data (get-in request [:params :data])
        _ (clear app model-spec)
        result (map one-result (insert app model-spec request data))
        errors (filter :errors result)
        status (if (empty? errors) 200 error-status)]
     (println "bulk-import/create inserts:" (count result) "errors:" (count errors))
     (if (not-empty errors) (clojure.pprint/pprint errors))
     {:body {:result result} :status status}))
