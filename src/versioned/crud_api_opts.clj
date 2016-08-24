(ns versioned.crud-api-opts
  (:require [versioned.model-support :refer [id-attribute]]
            [schema.core :as s]
            [versioned.schema :refer [Map Model Request]]))

(s/defn list-sort :- Map
  [model :- Model, request :- Request]
  (if (= (id-attribute model) :id)
    {:sort (array-map :id -1)}
    {}))

(s/defn list-opts :- Map
  [model :- Model, request :- Request]
  (merge (:query-params request)
         (list-sort model request)))

(s/defn get-opts :- Map
  [request :- Request]
  (:query-params request))
