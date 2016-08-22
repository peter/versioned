(ns versioned.model-indexes
  (:require [versioned.db-api :as db-api]
            [versioned.components.config :as config]
            [versioned.model-support :as model-support]
            [clojure.spec :as s]))

(s/fdef ensure-indexes
  :args (s/cat :database ::db-api/database :models ::config/models)
  :ret nil?)

(defn ensure-indexes [database models]
  (doseq [spec (filter #(:indexes %) (vals models))]
    (doseq [options (:indexes spec)]
      (let [coll (or (:coll options) (model-support/coll spec))
            fields (:fields options)
            options (dissoc options :coll :fields)]
        (db-api/ensure-index database coll fields options)))))
