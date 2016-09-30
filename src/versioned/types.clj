(ns versioned.types
  (:require [schema.core :as s]
            [versioned.util.core :as u]))

(def Map {s/Keyword s/Any})
(def Nil (s/pred nil? 'nil?))
(def Function (s/pred fn? 'fn?))
(def StrOrKeyword (s/cond-pre s/Str s/Keyword))
(def Coll (s/pred coll? 'coll?))

(def PosInt (s/pred u/positive-int? 'positive-int?))

(def ID (s/cond-pre s/Str PosInt))

(def LogLevel (s/enum "info" "debug"))

(def Request Map)

(def Attributes Map)
(def AttributeSet #{s/Keyword})

(def Changelog Map)
(def Action (s/pred #{:create :update :delete} 'changelog-action?))
(def Email s/Str)

(def JsonApiAttributes {
                        :id s/Str
                        (s/optional-key :type) s/Str
                        :attributes Map})
(def JsonApiData {
                  :data [JsonApiAttributes]})
(def JsonApiResource (merge JsonApiAttributes {
                                                (s/optional-key :relationships) {s/Keyword JsonApiData}}))
(def JsonApiError {
                   :type s/Str
                   (s/optional-key :message) s/Str
                   s/Keyword s/Any})
(def JsonApiResponse {
                      :status s/Int
                      (s/optional-key :body) Map})

(def JsonApiErrorResponse {:body {:errors [JsonApiError]} :status s/Int})
(def JsonApiDataResponse {:body {:data Coll} :status s/Int})

(def crud-actions [:list :get :create :update :delete])
(defn valid-routes? [routes]
  (empty? (clojure.set/difference (set routes)
                                  (set crud-actions))))

(declare Schema)
(declare SchemaValue)
(def SchemaMap {s/Keyword (s/recursive #'SchemaValue)})
(def SchemaArray [(s/recursive #'SchemaValue)])
(def SchemaValue (s/cond-pre s/Str s/Num Nil s/Bool SchemaMap SchemaArray))
(def SchemaType (s/enum "string" "number" "integer" "null" "boolean" "array" "object"))
(def SchemaMeta {
                 (s/optional-key :api_writable) s/Bool
                 (s/optional-key :api_readable) s/Bool
                 (s/optional-key :change_tracking) s/Bool
                 (s/optional-key :versioned) s/Bool
                 s/Keyword SchemaValue})
(def Schema {
             (s/optional-key :type) (s/cond-pre SchemaType [SchemaType])
             (s/optional-key :properties) {s/Keyword (s/recursive #'Schema)}
             (s/optional-key :additionalProperties) s/Bool
             (s/optional-key :required) [StrOrKeyword]
             (s/optional-key :items) (s/recursive #'Schema)
             (s/optional-key :enum) [SchemaValue]
             (s/optional-key :meta) SchemaMeta
             s/Keyword SchemaValue})

(def CallbackFunction Function)
(def CallbackSort (s/enum :first :middle :last))
(def CallbackMap {
                  :fn CallbackFunction
                  (s/optional-key :sort) CallbackSort})
(def Callback (s/cond-pre CallbackFunction CallbackMap))
(def BeforeAfterCallbacks {
                           (s/optional-key :before) [Callback]
                           (s/optional-key :after) [Callback]})
(def CallbackAction (s/enum :create :update :delete))
(def Callbacks {
                (s/optional-key :save) BeforeAfterCallbacks
                (s/optional-key :create) BeforeAfterCallbacks
                (s/optional-key :update) BeforeAfterCallbacks
                (s/optional-key :delete) BeforeAfterCallbacks})
(def CallbackOptions Map)
            ; :app app
            ; :config (:config app)
            ; :database (:database app)
            ; :action action
            ; :model-spec model-spec
            ; :schema (:schema model-spec))

; TODO: this spec is a duplicate of the JSON schema in model_spec.clj
(def Routes (s/pred valid-routes? 'valid-routes?))
(def Model {
            :type s/Keyword
            :schema Schema
            (s/optional-key :callbacks) Map
            (s/optional-key :relationships) Map
            (s/optional-key :indexes) [Map]
            (s/optional-key :routes) Routes
            s/Keyword s/Any})

(def ModelWriteFn Function) ; App -> Model -> Doc -> Doc

(def Doc Map) ; A model instance

(def Models {s/Keyword Model})

(def DB-Schema (s/pred #(instance? com.mongodb.DB %) 'mongodb-database?))
(def DB-Conn (s/pred #(instance? com.mongodb.MongoClient %) 'mongodb-conn?))
(def WriteResult (s/pred #(instance? com.mongodb.WriteResult %) 'write-result?))
(def DB-IndexOptions {
                      (s/optional-key :unique) s/Bool
                      (s/optional-key :name) s/Str})

(def Database {:db DB-Schema :conn DB-Conn s/Keyword s/Any})

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
             s/Keyword s/Any})


(def App {
          :config Config
          :models Models
          :swagger Map
          :routes [Route]
          s/Keyword s/Any})
