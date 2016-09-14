(ns versioned.crud-api-query
  (:require [clojure.string :as str]
            [versioned.util.core :as u]
            [versioned.model-schema :refer [deep-child-schema attribute-type]]
            [versioned.crud-api-types :refer [coerce-value]]))

(def query-pattern #"^(?:(\W*):)?([a-zA-Z0-9._-]+):(.+)$")

(def default-sep ",")

(defn parse-query [query-spec]
  (if-let [match (re-matches query-pattern query-spec)]
    (let [sep (or (get match 1) default-sep)]
      {
        :sep sep
        :field (get match 2)
        :values (str/split (get match 3) (u/str-to-regex sep))
      })
    nil))

(defn coerce-query-types [schema query]
    (reduce (fn [result [attribute values]]
              (let [type (attribute-type (deep-child-schema schema attribute) attribute)]
                (if (= type "array")
                  (assoc result attribute (coerce-value schema attribute values))
                  (assoc result attribute (map (partial coerce-value schema attribute) values)))))
            {}
            query))

(defn to-mongo-query [schema query]
  (reduce (fn [result [attribute values]]
            (let [type (attribute-type (deep-child-schema schema attribute) attribute)]
              (if (> (count values) 1)
                (assoc result attribute {:$in values})
                (assoc result attribute (first values)))))
          {}
          query))

(defn list-query [model-spec request]
  (let [schema (:schema model-spec)
        query-specs (u/array (get-in request [:query-params :q] []))
        query (reduce (fn [result query-spec]
                        (if-let [q (parse-query query-spec)]
                          (assoc result (:field q)
                                        (concat (get result (:field q) []) (:values q)))
                          result))
                      {}
                      query-specs)]
    (->> query
         (coerce-query-types schema)
         (to-mongo-query schema))))
