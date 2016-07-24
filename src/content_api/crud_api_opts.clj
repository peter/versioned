(ns content-api.crud-api-opts
  (:require [content-api.util.core :as u]
            [content-api.model-support :refer [id-attribute]]))

; TODO: query parameter validation - use type coercion plus json schema
(defn- pagination [request]
  (->> (select-keys (get-in request [:params]) [:page :per-page])
       (u/map-values u/safe-parse-int)
       (u/compact)))

(defn- sort [model-spec request]
  (if (= (id-attribute model-spec) :id)
    {:sort (array-map :id -1)}
    {}))

(defn list-opts [model-spec request]
  (merge (pagination request)
         (sort model-spec request)))

; TODO: query parameter validation - use type coercion plus json schema
(defn get-opts [request]
  (select-keys (:params request) [:relationships :version :published]))
