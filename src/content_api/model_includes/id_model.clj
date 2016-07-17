(ns content-api.model-includes.id-model
  (:require [content-api.model-api :as model-api]))

(defn- next-id [app model-spec]
  (let [docs (model-api/find app model-spec {} {:per-page 1 :fields [:id] :sort (array-map :id -1)})]
    (inc (or (:id (first docs)) 0))))

(defn id-callback [doc options]
  (let [id (next-id (:app options) (:model-spec options))]
    (assoc doc :id id)))

(def id-schema {
  :type "object"
  :properties {
    :_id {:type "string"}
    :id {:type "integer"}
  }
  :required [:id]
})

(def id-callbacks {
  :create {
    :before [id-callback]
  }
})

(def id-indexes [
  {:fields [:id] :unique true}
])

(defn id-spec [& options] {
  :schema id-schema
  :callbacks id-callbacks
  :indexes id-indexes
})
