(ns versioned.model-indexes
  (:require [versioned.model-support :as model-support]
            [versioned.model-init :refer [get-models]]
            [versioned.db-api :as db-api]
            [versioned.logger :as logger]
            [schema.core :as s]
            [versioned.types :refer [Nil App]]))

(s/defn ensure-indexes :- Nil
  [app :- App]
  (doseq [spec (filter #(:indexes %) (vals (get-models app)))]
    (doseq [options (:indexes spec)]
      (let [coll (or (:coll options) (model-support/coll spec))
            fields (:fields options)
            options (dissoc options :coll :fields)]
        (logger/debug app "model-indexes/ensure-indexes" coll fields options)
        (db-api/ensure-index (:database app) coll fields options)))))
