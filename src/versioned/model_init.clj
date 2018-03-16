(ns versioned.model-init
  (:require [versioned.util.core :as u]
            [versioned.util.schema :refer [merge-schemas]]
            [schema.core :as s]
            [versioned.db-api :as db]
            [versioned.model-spec :refer [generate-spec]]
            [versioned.types :refer [Schema Doc Ref Models Model ModelOrPath Config Database DbModel]]))

(s/defn apply-db-schema :- Models
  [models :- Models
   db-model :- DbModel]
  (let [model-type (keyword (:model_type db-model))
        db-schema (:schema db-model)
        schema (get-in models [model-type :schema])]
    (if schema
      (let [updated-schema (merge-schemas db-schema schema)]
        (assoc-in models [model-type :schema] updated-schema))
      models)))

(s/defn with-source-path :- Model
  [model :- Model
   spec :- ModelOrPath]
  (if (instance? String spec)
    (assoc-in model [:schema :x-meta :source_path] spec)
    model))

(s/defn load-model-spec :- Model
  [config :- Config
   spec :- ModelOrPath]
  (if (string? spec)
    (let [spec-fn (u/load-var spec)
          loaded-model (spec-fn config)
          model-with-meta (with-source-path loaded-model spec)]
      model-with-meta)
    spec))

(s/defn load-db-model :- Model
  [doc :- Doc]
  (let [base-path (str "versioned.base-models." (:base_model doc) "/spec")
        base-fn (u/load-var base-path)
        base-config (base-fn (:model_type doc))
        model-type (keyword (:model_type doc))
        model-config {:type model-type :schema (:schema doc)}
        model (generate-spec base-config model-config)]
    model))

(s/defn init-src-models :- Models
  [config :- Config]
  (reduce (fn [models [type spec]]
                (assoc models type (load-model-spec config spec)))
                {}
                (:models config)))

(s/defn init-db-models :- Models
  [db-models-list :- [Doc]]
  (let [models-list (map load-db-model (filter :base_model db-models-list))]
    (reduce #(assoc %1 (keyword (:type %2)) %2) {} models-list)))

(s/defn init-models :- Models
  [config :- Config
   database :- Database]
  (let [src-models (init-src-models config)
        db-models-list (db/find database :models {})
        src-models-with-db-schema (reduce apply-db-schema src-models db-models-list)
        db-models (init-db-models db-models-list)
        models (merge db-models src-models-with-db-schema)]
    models))
