(ns versioned.model-relationships
  (:require [versioned.model-support :as model-support]
            [versioned.model-versions :refer [apply-version versioned-coll published-model?]]
            [versioned.db-api :as db]
            [versioned.model-init :refer [get-model]]
            [schema.core :as s]
            [versioned.types :refer [RelationshipKey
                                     RelationshipField
                                     Relationship
                                     Relationships
                                     Model
                                     Map
                                     App
                                     Doc]]
            [versioned.util.core :as u]))

(s/defn id-field :- RelationshipField
  [relationship :- RelationshipKey]
  (keyword (str (name relationship) "_id")))

(s/defn ids-field :- RelationshipField
  [relationship :- RelationshipKey]
  (keyword (str (name relationship) "_ids")))

(s/defn relationship-spec :- Relationship
  [model-spec :- Map
   relationship :- RelationshipKey]
  (let [options (get-in model-spec [:relationships relationship])
        from-model (get options :from_model (:type model-spec))
        from-coll (get options :from_coll (:type model-spec))
        attribute-keys (set (keys (get-in model-spec [:schema :properties])))
        default-from-field (some attribute-keys [(id-field relationship) (ids-field relationship)])
        from-field (get options :from_field default-from-field)
        to-model (get options :to_model relationship)
        to-coll (get options :to_coll relationship)
        to-field (get options :to_field :id)]
    (merge options {
      :from_model from-model
      :from_coll from-coll
      :from_field from-field
      :to_model to-model
      :to_coll to-coll
      :to_field to-field
    })))

(s/defn normalized-relationships :- (s/maybe Relationships)
  [model-spec :- Map]
  (not-empty (reduce (fn [m [k v]] (assoc m k (relationship-spec model-spec k)))
                     {}
                     (get model-spec :relationships {}))))

(s/defn with-published-versions :- [Doc]
  [app :- App
   docs :- [Doc]
   model :- s/Keyword
   opts :- Map]
  (if (and (:published opts) (published-model? app model))
    (let [docs (filter :published_version docs)
          draft-docs (filter #(not= (:published_version %) (:version %)) docs)
          version-ids (map #(hash-map :id (:id %) :version (:published_version %)) draft-docs)
          versions-query {:$or version-ids}
          versions-spec (get-model app model)
          versions-coll (versioned-coll versions-spec)
          versions (if (not-empty version-ids) (db/find (:database app) versions-coll versions-query))
          versions-by-id (reduce #(assoc %1 (:id %2) %2) {} versions)
          published-docs (map (fn [doc]
                                (if-let [version (get versions-by-id (:id doc))]
                                  (apply-version versions-spec doc version)
                                  doc))
                              docs)]
      published-docs)
    docs))

; Example: (i.e. ActiveRecord has_and_belongs_to_many or has_many :through)
; from_coll pages
; from_field widgets_ids
; to_coll widgets
; to_field id
(s/defn find-relationship-to-many :- [Doc]
  [app :- App
   model :- Model
   doc :- Doc
   relationship :- RelationshipKey
   opts :- Map]
  (let [spec (get-in model [:relationships (keyword relationship)])
        coll (:to_coll spec)
        model (:to_model spec)
        field (:to_field spec)
        ids ((:from_field spec) doc)
        query {field {:$in ids}}
        default-find-opts {:per-page (count ids)}
        find-opts (merge default-find-opts (:find_opts spec))
        docs (and (not-empty ids) (db/find (:database app) coll query find-opts))
        docs-by-id (group-by field docs)
        ordered-docs (u/compact (map #(first (docs-by-id %)) ids))]
    (with-published-versions app ordered-docs model opts)))

; Example: (i.e. ActiveRecord belongs_to)
; from_coll pages
; from_field widgets_id
; to_coll widgets
; to_field id
(s/defn find-relationship-to-one :- [Doc]
  [app :- App
   model :- Model
   doc :- Doc
   relationship :- RelationshipKey
   opts :- Map]
  (let [spec (get-in model [:relationships (keyword relationship)])
        coll (:to_coll spec)
        model (:to_model spec)
        field (:to_field spec)
        id ((:from_field spec) doc)
        query {field id}
        find-opts (:find_opts spec)
        docs (and id (db/find (:database app) coll query find-opts))]
    (first (with-published-versions app docs model opts))))

; Example: (i.e. ActiveRecord has_many)
; from_coll pages_versions
; from_field id
; to_coll pages
; to_field id
(s/defn find-relationship-from-many :- [Doc]
  [app :- App
   model :- Model
   doc :- Doc
   relationship :- RelationshipKey
   opts :- Map]
  (let [spec (get-in model [:relationships (keyword relationship)])
        coll (:from_coll spec)
        model (:from_model spec)
        field (:from_field spec)
        id ((:to_field spec) doc)
        query {field id}
        find-opts (:find_opts spec)
        docs (and id (db/find (:database app) coll query find-opts))]
    (with-published-versions app docs model opts)))

(s/defn find-relationship :- [Doc]
  [app :- App
   model :- Model
   doc :- Doc
   relationship :- RelationshipKey
   opts :- Map]
  (let [spec (get-in model [:relationships (keyword relationship)])
        coll (model-support/coll model)
        from-field (:from_field spec)
        multiple? (= (get-in model [:schema :properties from-field :type]) "array")
        find-fn (cond
                  (and (= coll (:from_coll spec)) multiple?) find-relationship-to-many
                  (and (= coll (:from_coll spec)) (not multiple?)) find-relationship-to-one
                  (= coll (:to_coll spec)) find-relationship-from-many)]
    (find-fn app model doc relationship opts)))

(s/defn with-relationships :- (s/maybe Doc)
  [app :- App
   model :- Model
   doc :- (s/maybe Doc)
   opts :- Map]
  (if (and doc (not-empty (:relationships model)) (:relationships opts))
    (let [spec (if (:published opts) (dissoc (:relationships model) :versions)
                                      (:relationships model))
          relationships (u/compact (u/map-key-values #(find-relationship app model doc % opts)
                                                     spec))]
      (with-meta doc (merge (meta doc) {:relationships relationships})))
    doc))
