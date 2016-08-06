(ns versioned.middleware.params-parser
  (:require [versioned.util.core :as u]
            [versioned.crud-api-opts :refer [query-parameters query-params-schema]]
            [versioned.util.schema :refer [validate-schema]]
            [versioned.crud-api-types :refer [coerce-attribute-types]]
            [versioned.json-api :refer [error-response]]))

(defn wrap-params-parser [handler app]
  (fn [request]
    (if (not-empty (query-parameters (get-in request [:route :swagger])))
      (let [swagger (get-in request [:route :swagger])
            schema (query-params-schema swagger)
            query-params (coerce-attribute-types schema (u/keywordize-keys (:query-params request)))
            request-with-params (assoc request :query-params query-params
                                               :params (merge (:params request) query-params))
            errors (validate-schema schema query-params)]
        (if errors
          (error-response errors)
          (handler request-with-params)))
      (handler request))))
