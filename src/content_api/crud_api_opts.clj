(ns content-api.crud-api-opts
  (:require [content-api.util.core :as u]))

; TODO: query parameter validation - use type coercion plus json schema
(defn- pagination [request]
  (->> (select-keys (get-in request [:params]) [:page :per-page])
       (u/map-values u/safe-parse-int)
       (u/compact)))

(defn list-opts [request]
  (pagination request))

; TODO: query parameter validation - use type coercion plus json schema
(defn get-opts [request]
  (select-keys (:params request) [:relationships :version :published]))
