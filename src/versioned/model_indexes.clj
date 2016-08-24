(ns versioned.model-indexes
  (:require [versioned.db-api :as db-api :refer [Database]]
            [versioned.components.config :as config :refer [Models]]
            [versioned.model-support :as model-support]
            [schema.core :as s]
            [versioned.schema :refer [Nil]]))

(s/defn ensure-indexes :- Nil
  [database :- Database, models :- Models]
  (doseq [spec (filter #(:indexes %) (vals models))]
    (doseq [options (:indexes spec)]
      (let [coll (or (:coll options) (model-support/coll spec))
            fields (:fields options)
            options (dissoc options :coll :fields)]
        (db-api/ensure-index database coll fields options)))))
