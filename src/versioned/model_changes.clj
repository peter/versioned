(ns versioned.model-changes
  (:require [schema.core :as s]
            [versioned.types :refer [Model
                                     Attribute
                                     Doc
                                     Schema
                                     ModelChanges
                                     AttributeSet]]))

(s/defn changed-value? :- s/Bool
  [from-value :- s/Any
   to-value :- s/Any]
  (not= from-value to-value))

(s/defn tracked-attribute? :- s/Bool
  [attribute-schema :- Schema]
  (get-in attribute-schema [:meta :change_tracking] true))

(s/defn tracked-attributes :- AttributeSet
  [model :- Model]
  (let [schema (:schema model)]
    (set (filter #(tracked-attribute? (% (:properties schema)))
            (keys (:properties schema))))))

(s/defn model-changes :- (s/maybe ModelChanges)
  [model :- Model
   doc :- Doc]
  (let [from (get (meta doc) :existing-doc {})
        to doc
        changed-attributes (filter #(changed-value? (% from) (% to)) (tracked-attributes model))]
    (not-empty (into {} (map #(vector % {:from (% from) :to (% to)}) changed-attributes)))))

(s/defn model-changed? :- s/Bool
  ([model :- Model
    doc :- Doc
    attribute :- Attribute
    from :- s/Any
    to :- s/Any]
    (let [change (attribute (model-changes model doc))]
      (boolean (and change
                    (= from (:from change))
                    (= to (:to change))))))
  ([model :- Model
    doc :- Doc
    attribute :- Attribute]
    (boolean (attribute (model-changes model doc))))
  ([model :- Model
    doc :- Doc]
    (boolean (not-empty (model-changes model doc)))))
