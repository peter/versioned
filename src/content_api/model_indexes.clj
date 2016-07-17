(ns content-api.model-indexes
  (:require [content-api.db-api :as db]
            [content-api.model-support :as model-support]))

(defn ensure-indexes [database models]
  (doseq [spec (filter #(:indexes %) (vals models))]
    (doseq [options (:indexes spec)]
      (let [coll (or (:coll options) (model-support/coll spec))
            fields (:fields options)
            options (dissoc options :coll :fields)]
        (db/ensure-index database coll fields options)))))
