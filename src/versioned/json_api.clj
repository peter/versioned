(ns versioned.json-api
  (:require [versioned.util.core :as u]
            [versioned.model-support :as model-support]
            [versioned.model-validations :refer [model-errors model-not-updated]]))

; Inspired by http://jsonapi.org

(def error-status 422)

(defn id [request]
  (get-in request [:params :id]))

(defn attributes [model-spec request]
  (get-in request [:params :data :attributes]))

(defn with-attrs [model-spec doc]
  (let [id ((model-support/id-attribute model-spec) doc)]
    {
      :id (str id)
      :type (:type doc)
      :attributes doc
    }))

(defn relationship-docs [model-spec docs]
  {:data (map #(with-attrs model-spec %) docs)})

(defn resource [model-spec doc]
  (let [id ((model-support/id-attribute model-spec) doc)
        relationships (not-empty (u/map-values (partial relationship-docs model-spec)
                                               (or (:relationships (meta doc)) {})))]
    (u/compact (merge (with-attrs model-spec doc) {
      :relationships relationships
    }))))

(defn error-response [errors]
  {:body {:errors errors} :status error-status})

(defn data-response
  ([data status] {:body {:data data} :status status})
  ([data] (data-response data 200)))

(defn no-update-response []
  {:status 204})

(defn missing-response []
  {:body {} :status 404})

(defn invalid-attributes-response [invalids]
  (error-response [{:type "invalid_attributes" :attributes invalids}]))

(defn doc-response [model-spec doc]
  (let [errors (model-errors doc)
        data (resource model-spec doc)]
  (cond
    (= errors model-not-updated) (no-update-response)
    errors (error-response errors)
    :else (data-response data))))

(defn docs-response [model-spec docs]
  (let [data (map (partial resource model-spec) docs)]
    (data-response data)))
