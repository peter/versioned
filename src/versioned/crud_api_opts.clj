(ns versioned.crud-api-opts
  (:require [versioned.model-support :refer [id-attribute]]
            [schema.core :as s]
            [clojure.string :as str]
            [versioned.schema :refer [Map Model Request]]))

(s/defn default-list-sort :- Map
  [model :- Model, request :- Request]
  (if (= (id-attribute model) :id)
    {:sort (array-map :id -1)}
    {}))

(s/defn list-sort :- Map
  [model :- Model request :- Request]
  (if-let [sort (get-in request [:query-params :sort])]
    (let [specs (str/split sort #",")]
      (reduce (fn [result spec]
                (let [[_ prefix field] (re-matches #"^(-?)(.+)$" spec)
                      order (if (= prefix "-") -1 1)]
                  (assoc-in result [:sort field] order)))
              {:sort (array-map)}
              specs))
    (default-list-sort model request)))

(s/defn list-opts :- Map
  [model :- Model, request :- Request]
  (merge (:query-params request)
         (list-sort model request)))

(s/defn get-opts :- Map
  [request :- Request]
  (:query-params request))
