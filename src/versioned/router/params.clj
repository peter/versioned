(ns versioned.router.params
  (:require [clojure.string :as str]))

(def name-pattern #":([a-z0-9-]+)")

(defn param-names [path]
  (map (comp keyword second) (re-seq name-pattern path)))

(def value-pattern #"([^/?]+)")

(defn path-pattern [path]
  (re-pattern (str/replace path name-pattern (.toString value-pattern))))

(defn params [names path uri]
  (not-empty (zipmap names (rest (re-find (path-pattern path) uri)))))
