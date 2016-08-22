(ns versioned.model-indexes
  (:require [versioned.db-api :as db]
            [versioned.model-support :as model-support]
            [clojure.spec :as s]))

(defn ensure-indexes [database models]
  (doseq [spec (filter #(:indexes %) (vals models))]
    (doseq [options (:indexes spec)]
      (let [coll (or (:coll options) (model-support/coll spec))
            fields (:fields options)
            options (dissoc options :coll :fields)]
        (db/ensure-index database coll fields options)))))

(s/def :versioned/database any?) ; TODO
(s/def :versioned/model any?) ; TODO
(s/def :versioned/config/models (s/map-of keyword? :versioned/model))

(s/fdef ensure-indexes
  :args (s/cat :database :versioned/database :models :versioned/config/models)
  :ret nil?)
