(ns versioned.swagger.ref
  (:require [clojure.string :as str]
            [versioned.util.core :as u]))

(defn get-ref [selection]
  (if (map? selection)
    (get selection "$ref")
    nil))

(defn ref-path [ref]
  (map keyword (drop 1 (str/split ref #"/"))))

(defn resolve-ref [swagger-spec selection]
  (if-let [ref (get-ref selection)]
    (get-in swagger-spec (ref-path ref))
    selection))

(defn deep-resolve-ref [swagger-spec]
  (let [recurse-if? #(and (coll? (:value %))
                          (not (get-ref (:value %))))]
    (u/deep-map-values #(resolve-ref swagger-spec (:value %))
                       swagger-spec
                       {:recurse-if? recurse-if?})))
