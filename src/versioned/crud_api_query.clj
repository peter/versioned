(ns versioned.crud-api-query
  (:require [clojure.string :as str]
            [schema.core :as s]
            [versioned.types :refer [Map Schema Model Request]]
            [versioned.util.core :as u]
            [versioned.model-schema :refer [deep-child-schema attribute-type]]
            [versioned.crud-api-types :refer [coerce-value]]))

(def query-pattern #"^(?:(\W*):)?([a-zA-Z0-9._-]+):(.+)$")

(def default-sep ",")

(s/defn parse-query :- (s/maybe Map)
  [query-spec :- String]
  (if-let [match (re-matches query-pattern query-spec)]
    (let [sep (or (get match 1) default-sep)]
      {
        :sep sep
        :field (keyword (get match 2))
        :values (str/split (get match 3) (u/str-to-regex sep))})

    nil))

(s/defn coerce-query-types :- Map
  [schema :- Schema query :- Map]
  (reduce (fn [result [attribute values]]
            (let [type (attribute-type (deep-child-schema schema attribute) attribute)]
              (if (= type "array")
                (assoc result attribute (coerce-value schema attribute values))
                (assoc result attribute (map (partial coerce-value schema attribute) values)))))
          {}
          query))

(s/defn to-mongo-query :- Map
  [schema :- Schema query :- Map]
  (reduce (fn [result [attribute values]]
            (let [type (attribute-type (deep-child-schema schema attribute) attribute)]
              (if (> (count values) 1)
                (assoc result attribute {:$in values})
                (assoc result attribute (first values)))))
          {}
          query))

(s/defn list-query :- Map
  [model :- Model request :- Request]
  (let [schema (:schema model)
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
