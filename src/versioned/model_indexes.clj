(ns versioned.model-indexes
  (:require [versioned.model-support :as model-support]
            [versioned.db-api :as db-api]
            [schema.core :as s]
            [versioned.schema :refer [Nil Database Models]]))

(s/defn ensure-indexes :- Nil
  [database :- Database, models :- Models]
  (doseq [spec (filter #(:indexes %) (vals models))]
    (doseq [options (:indexes spec)]
      (let [coll (or (:coll options) (model-support/coll spec))
            fields (:fields options)
            options (dissoc options :coll :fields)]
        (db-api/ensure-index database coll fields options)))))
