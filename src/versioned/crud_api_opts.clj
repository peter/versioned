(ns versioned.crud-api-opts
  (:require [versioned.model-support :refer [id-attribute]]))

(defn- list-sort [model-spec request]
  (if (= (id-attribute model-spec) :id)
    {:sort (array-map :id -1)}
    {}))

(defn list-opts [model-spec request]
  (merge (:query-params request)
         (list-sort model-spec request)))

(defn get-opts [request]
  (:query-params request))
