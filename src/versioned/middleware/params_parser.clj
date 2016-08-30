(ns versioned.middleware.params-parser
  (:require [versioned.util.core :as u]
            [versioned.swagger.parameters :refer [parameters-in parameters-schema arrayify-attributes]]
            [versioned.util.schema :refer [validate-schema]]
            [versioned.crud-api-types :refer [coerce-attribute-types]]
            [versioned.json-api :refer [error-response]]))

(defn wrap-params-parser [handler app]
  (fn [request]
    (let [swagger (get-in request [:route :swagger])
          parameters (u/compact {
            :query-params (parameters-in swagger "query")
            :path-params (parameters-in swagger "path")
          })]
    (if (not-empty parameters)
      (let [in-params (reduce (fn [result [key parameters]]
                                (let [schema (parameters-schema parameters)
                                      attributes (->> (key request)
                                                      (u/keywordize-keys)
                                                      (arrayify-attributes schema))]
                                  (assoc result key (coerce-attribute-types schema attributes))))
                              {}
                              parameters)
            params (apply merge (:params request) (vals in-params))
            request-with-params (apply assoc request :params params (flatten (seq in-params)))
            errors (u/compact (reduce (fn [result [key attributes]]
                                        (let [schema (parameters-schema (key parameters))]
                                          (concat result (validate-schema schema attributes))))
                                      []
                                      in-params))]
        (if (not-empty errors)
          (error-response errors)
          (handler request-with-params)))
      (handler request)))))
