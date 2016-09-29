(ns versioned.crud-api-attributes
  (:require [versioned.model-support :as model-support]
            [versioned.json-api :as json-api]
            [versioned.model-attributes :refer [api-writable-attributes api-readable-attributes]]
            [versioned.crud-api-audit :refer [updated-by created-by save-changelog]]
            [versioned.crud-api-types :refer [coerce-attribute-types]]
            [schema.core :as s]
            [versioned.types :refer [Model Attributes AttributeKeys Request]]))

(s/defn write-attributes :- Attributes
  [model :- Model, attributes :- Attributes]
  (->> attributes
       (api-writable-attributes (:schema model))
       (coerce-attribute-types (:schema model))))

(s/defn read-attributes :- Attributes
  [model :- Model, attributes :- Attributes]
  (->> attributes
       (api-readable-attributes (:schema model))))

(s/defn create-attributes :- Attributes
  [model :- Model, request :- Request, attributes :- Attributes]
  (merge (write-attributes model attributes)
         (created-by request)))

(s/defn update-attributes :- Attributes
  [model :- Model, request :- Request, attributes :- Attributes]
  (merge (write-attributes model attributes)
         (model-support/id-query model (json-api/id request))
         (updated-by request)))

(s/defn invalid-attributes :- (s/maybe AttributeKeys)
  [model :- Model, request :- Request]
  (not-empty (clojure.set/difference (set (keys (json-api/attributes model request)))
                                     (set (keys (get-in model [:schema :properties]))))))
