(ns content-api.json-api
  (:require [content-api.util.core :as u]
            [content-api.model-support :as model-support]
            [content-api.model-validations :refer [model-errors model-not-updated]]))

; Inspired by http://jsonapi.org

(defn id [request]
  (get-in request [:params :id]))

(defn attributes [model-spec request]
  (get-in request [:params (:type model-spec)]))

(defn with-attrs [model-spec doc]
  (let [id ((model-support/id-attribute model-spec) doc)]
    {
      :id (str id)
      :type (:type doc)
      :attributes doc
    }))

(defn relationship-docs [model-spec docs]
  {:data (map #(with-attrs model-spec %) docs)})

(defn json-doc [model-spec doc]
  (let [id ((model-support/id-attribute model-spec) doc)
        relationships (not-empty (u/map-values (partial relationship-docs model-spec)
                                               (or (:relationships (meta doc)) {})))]
    (u/compact (merge (with-attrs model-spec doc) {
      :relationships relationships
    }))))

(defn error-response [errors]
  {:body {:errors errors} :status 422})

(defn data-response
  ([data status] {:body {:data data} :status status})
  ([data] (data-response data 200)))

(defn no-update-response [data]
  {:status 204})

(defn missing-response []
  {:body {} :status 404})

(defn invalid-attributes-response [invalids]
  (error-response [{:type "invalid_attributes" :attributes invalids}]))

(defn response [doc]
  (cond
    (= (model-errors doc) model-not-updated) (no-update-response doc)
    (model-errors doc) (error-response (model-errors doc))
    :else (data-response doc)))
