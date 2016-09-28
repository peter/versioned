(ns versioned.schema
  (:require [schema.core :as s]))

(def Map {s/Keyword s/Any})
(def Nil (s/pred nil? 'nil?))
(def Function (s/pred fn? 'fn?))

(def Request Map)

(def Attributes Map)
(def AttributeKeys #{s/Keyword})

(def Changelog Map)
(def Action (s/pred #{:create :update :delete} 'changelog-action?))
(def Email s/Str)

(def crud-actions [:list :get :create :update :delete])
(defn valid-routes? [routes]
  (empty? (clojure.set/difference (set routes)
                                  (set crud-actions))))

; TODO: this spec is a duplicate of the JSON schema in model_spec.clj
(def Routes (s/pred valid-routes? 'valid-routes?))
(def Model {
  :type s/Keyword
  :schema Map
  (s/optional-key :callbacks) Map
  (s/optional-key :relationships) Map
  (s/optional-key :indexes) [Map]
  (s/optional-key :routes) Routes
  s/Keyword s/Any
})

(def Models {s/Keyword Model})

(def DB-Schema (s/pred #(instance? com.mongodb.DB %) 'mongodb-database?))
(def Database {:db DB-Schema s/Keyword s/Any})

(def Route Map)

(def Handler Function)

(def ModelSpecPath (s/constrained String #(re-matches #"^([\w.-]+)/([\w-]+)$" %))) ; "versioned.models.users/spec"

(def ModelsConfig (s/constrained
                    {s/Keyword (s/conditional map? Model :else ModelSpecPath)}
                    (fn [m]
                      (every? (fn [[k v]]
                                (or (string? v)
                                    (= k (:type v))))
                              (seq m)))))

(def Config {
  :models ModelsConfig
  s/Keyword s/Any
})

(def App {
  :config Config
  :models Models
  :swagger Map
  :routes [Route]
  s/Keyword s/Any
})
