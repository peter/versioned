(ns versioned.json-api
  (:require [versioned.util.core :as u]
            [versioned.model-support :as model-support]
            [clojure.string :as str]
            [schema.core :as s]
            [versioned.types :refer [Map
                                     ID
                                     Request
                                     Model
                                     JsonApiAttributes
                                     JsonApiData
                                     JsonApiResource
                                     JsonApiResponse
                                     JsonApiErrorResponse
                                     JsonApiDataResponse
                                     Coll
                                     AttributeSet
                                     JsonApiError]]
            [versioned.model-validations :refer [model-errors model-not-updated]]))

; Inspired by http://jsonapi.org

(def error-status 422)

(s/defn id :- ID
  [request :- Request]
  (get-in request [:params :id]))

(s/defn attributes :- (s/maybe Map)
  [request :- Request]
  (get-in request [:params :data :attributes]))

(s/defn json-api-attributes :- JsonApiAttributes
  [model :- Model
   doc :- Map]
  (let [id ((model-support/id-attribute model) doc)]
    (u/compact {
                :id (str id)
                :type (u/keyword-str (:type doc))
                :attributes doc})))

(s/defn json-api-data :- JsonApiData
  [model :- Model
   docs :- [Map]]
  {:data (map #(json-api-attributes model %) docs)})

(s/defn resource :- JsonApiResource
  [model :- Model
   doc :- Map]
  (let [id ((model-support/id-attribute model) doc)
        relationships (not-empty (u/map-values (partial json-api-data model)
                                               (or (:relationships (meta doc)) {})))]
    (u/compact (merge (json-api-attributes model doc) {
                                                       :relationships relationships}))))

(s/defn error-response :- JsonApiErrorResponse
  [errors :- [JsonApiError]]
  {:body {:errors errors} :status error-status})

(s/defn data-response :- JsonApiDataResponse
  ([data :- Coll status :- s/Int] {:body {:data data} :status status})
  ([data :- Coll] (data-response data 200)))

(s/defn no-update-response :- {:status s/Int}
  []
  {:status 204})

(s/defn missing-response :- {:status s/Int}
  []
  {:status 404})

(s/defn invalid-attributes-response :- JsonApiErrorResponse
  [invalids :- AttributeSet]
  (error-response [{
                    :type "invalid_attributes"
                    :attributes invalids
                    :message (str "Invalid attributes: " (str/join ", " invalids))}]))

(s/defn missing-attributes-response :- JsonApiErrorResponse
  []
  (error-response [{
                    :type "missing_attributes"
                    :message "Could not find data in request body"}]))

(s/defn doc-response :- JsonApiResponse
  [model :- Model
   doc :- Map]
  (let [errors (model-errors doc)
        data (resource model doc)]
   (cond
     (= errors model-not-updated) (no-update-response)
     errors (error-response errors)
     :else (data-response data))))

(s/defn docs-response :- JsonApiResponse
  [model :- Model
   docs :- [Map]]
  (let [data (map (partial resource model) docs)]
    (data-response data)))
