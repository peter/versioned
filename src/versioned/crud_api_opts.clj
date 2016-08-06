(ns versioned.crud-api-opts
  (:require [versioned.util.core :as u]
            [versioned.model-support :refer [id-attribute]]))

(defn query-parameters [swagger-spec]
  (let [parameters (get-in swagger-spec [:parameters] {})]
    (->> (filter #(= (% :in) "query") parameters)
         (map #(dissoc % :required :in :description)))))

(defn query-params-schema [swagger-spec]
  (let [parameters (query-parameters swagger-spec)
        required (not-empty (->> (filter #(= (% :required) true) parameters)
                                 (map :name)))
        properties (reduce #(assoc %1 (keyword (%2 :name)) (dissoc %2 :name)) {} parameters)]
    (u/compact {:type "object"
                :properties properties
                :additionalProperties false
                :required required})))

(defn- list-sort [model-spec request]
  (if (= (id-attribute model-spec) :id)
    {:sort (array-map :id -1)}
    {}))

(defn list-opts [model-spec request]
  (merge (:query-params request)
         (list-sort model-spec request)))

(defn get-opts [request]
  (:query-params request))
