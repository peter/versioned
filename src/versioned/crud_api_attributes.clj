(ns versioned.crud-api-attributes
  (:require [versioned.model-support :as model-support]
            [versioned.model-spec :as model-spec :refer [Model]]
            [versioned.json-api :as json-api]
            [versioned.model-attributes :refer [api-writable-attributes api-readable-attributes]]
            [versioned.crud-api-audit :refer [updated-by created-by save-changelog]]
            [versioned.crud-api-types :refer [coerce-attribute-types]]
            [clojure.spec :as s]
            [schema.core :as schema]))

(def Map {schema/Keyword schema/Any})
(def Attributes Map)
(def AttributeKeys #{schema/Keyword})
(def Request Map)

; (s/fdef write-attributes
;   :args (s/cat :model ::model-spec/model :attributes map?)
;   :ret map?)

(schema/defn write-attributes :- Attributes
  [model :- Model attributes :- Attributes]
  (->> attributes
       (api-writable-attributes (:schema model))
       (coerce-attribute-types (:schema model))))

; (s/fdef read-attributes
;   :args (s/cat :model ::model-spec/model :attributes map?)
;   :ret map?)

(schema/defn read-attributes :- Attributes
  [model :- Model attributes :- Attributes]
  (->> attributes
       (api-readable-attributes (:schema model))))

; (s/fdef create-attributes
;   :args (s/cat :model ::model-spec/model :request map? :attributes map?)
;   :ret map?)

(schema/defn create-attributes :- Attributes
  [model :- Model request :- Request attributes :- Attributes]
  (merge (write-attributes model attributes)
         (created-by request)))

; (s/fdef update-attributes
;   :args (s/cat :model ::model-spec/model :request map? :attributes map?)
;   :ret map?)

(schema/defn update-attributes :- Attributes
  [model :- Model request :- Request attributes :- Attributes]
  (merge (write-attributes model attributes)
         (model-support/id-query model (json-api/id request))
         (updated-by request)))

; (s/fdef invalid-attributes
;   :args (s/cat :model ::model-spec/model :request map?)
;   :ret map?)

(schema/defn invalid-attributes :- (schema/maybe AttributeKeys)
  [model :- Model request :- Request]
  (not-empty (clojure.set/difference (set (keys (json-api/attributes model request)))
                                     (set (keys (get-in model [:schema :properties]))))))
