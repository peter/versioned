(ns versioned.middleware.params-parser
  (:require [versioned.util.core :as u]
            [versioned.swagger.parameters :refer [parameters-in parameters-schema]]
            [versioned.util.schema :refer [validate-schema]]
            [versioned.crud-api-types :refer [coerce-attribute-types]]
            [versioned.json-api :refer [error-response]]))

(defn wrap-params-parser [handler app]
  (fn [request]
    (if (not-empty (parameters-in (get-in request [:route :swagger]) "query"))
      (let [swagger (get-in request [:route :swagger])
            schema (parameters-schema (parameters-in swagger "query"))
            query-params (coerce-attribute-types schema (u/keywordize-keys (:query-params request)))
            request-with-params (assoc request :query-params query-params
                                               :params (merge (:params request) query-params))
            errors (validate-schema schema query-params)]
        (if errors
          (error-response errors)
          (handler request-with-params)))
      (handler request))))
