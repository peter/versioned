(ns versioned.model-changes
  (:require [versioned.model-attributes :refer [api-writable?]]))

(defn changed-value? [from-value to-value]
  (not= from-value to-value))

(defn tracked-attributes [model-spec]
  (let [properties (get-in model-spec [:schema :properties])]
    (set (filter #(api-writable? (% properties)) (keys properties)))))

(defn model-changes [model-spec doc]
  (let [from (get (meta doc) :existing-doc {})
        to doc
        changed-attributes (filter #(changed-value? (% from) (% to)) (tracked-attributes model-spec))]
    (not-empty (into {} (map #(vector % {:from (% from) :to (% to)}) changed-attributes)))))

(defn model-changed?
  ([model-spec doc attribute from to]
    (let [change (attribute (model-changes model-spec doc))]
      (and change
           (= from (:from change))
           (= to (:to change)))))
  ([model-spec doc attribute]
    (attribute (model-changes model-spec doc)))
  ([model-spec doc]
    (not-empty (model-changes model-spec doc))))
