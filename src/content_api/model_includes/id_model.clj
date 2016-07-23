(ns content-api.model-includes.id-model
  (:require [content-api.model-api :as model-api]))

(defn- next-id [app model-spec]
  (let [docs (model-api/find app model-spec {} {:per-page 1 :fields [:id] :sort (array-map :id -1)})]
    (inc (or (:id (first docs)) 0))))

(defn id-callback [doc options]
  (if-not (:id doc)
    (let [id (next-id (:app options) (:model-spec options))]
      (assoc doc :id id))
    doc))

(def id-schema {
  :type "object"
  :properties {
    :_id {:type "string" :meta {:api_writable false}}
    :id {:type "integer" :meta {:api_writable false}}
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
