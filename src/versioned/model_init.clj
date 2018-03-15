(ns versioned.model-init
  (:require [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.db-api :as db]
            [versioned.types :refer [Schema Ref Models Model ModelOrPath Config Database DbModel]]))

(defn get-models [app]
  (deref (:models app)))

(defn get-model [app model]
  (get-in (get-models app) [model]))

(defn get-in-model [app path]
  (get-in (get-models app) path))

; NOTE: there is no official merge support for JSON schema AFAIK and we cannot use "allOf"
(s/defn merge-schemas :- Schema
  [& schemas :- [Schema]]
  (let [properties (apply merge (map :properties schemas))
        required (not-empty (apply concat (map :required schemas)))]
    (u/compact (assoc (apply u/deep-merge schemas)
                      :properties properties
                      :required required))))

(s/defn apply-db-model :- Models
  [models :- Models
   db-model :- DbModel]
  (let [model-type (keyword (:model_type db-model))
        db-schema (:schema db-model)
        schema (get-in models [model-type :schema])]
    (if schema
      (let [updated-schema (merge-schemas db-schema schema)]
        (assoc-in models [model-type :schema] updated-schema))
      models)))

(s/defn load-model-spec :- Model
  [config :- Config
   spec :- ModelOrPath]
  (if (string? spec)
    (let [spec-fn (u/load-var spec)
          loaded-model (spec-fn config)]
      loaded-model)
    spec))

(s/defn init-src-models :- Models
  [config :- Config]
  (reduce (fn [models [type spec]]
                (assoc models type (load-model-spec config spec)))
                {}
                (:models config)))

(s/defn init-models :- Models
  [config :- Config
   database :- Database]
  (let [src-models (init-src-models config)
        db-models (db/find database :models {})
        models (reduce apply-db-model src-models db-models)]
    models))
