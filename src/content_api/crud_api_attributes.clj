(ns content-api.crud-api-attributes
  (:require [content-api.model-support :as model-support]
            [content-api.json-api :as json-api]
            [content-api.model-attributes :refer [api-writable-attributes api-readable-attributes]]
            [content-api.crud-api-audit :refer [updated-by created-by save-changelog]]
            [content-api.crud-api-types :refer [coerce-attribute-types]]))

(defn- write-attributes [model-spec attributes]
  (->> attributes
       (api-writable-attributes (:schema model-spec))
       (coerce-attribute-types (:schema model-spec))))

(defn read-attributes [model-spec attributes]
  (->> attributes
       (api-readable-attributes (:schema model-spec))))

(defn create-attributes [model-spec request attributes]
  (merge (write-attributes model-spec attributes)
         (created-by request)))

(defn update-attributes [model-spec request attributes]
  (merge (write-attributes model-spec attributes)
         (model-support/id-query model-spec (json-api/id request))
         (updated-by request)))

(defn invalid-attributes [model-spec request]
  (not-empty (clojure.set/difference (set (keys (json-api/attributes model-spec request)))
                                     (set (keys (get-in model-spec [:schema :properties]))))))
