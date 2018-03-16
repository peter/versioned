(ns versioned.crud-api-audit
  (:require [versioned.model-validations :refer [model-errors]]
            [versioned.util.date :as d]
            [versioned.model-api :as model-api]
            [versioned.model-changes :refer [model-changes]]
            [versioned.util.model :refer [get-model]]
            [versioned.db-api :as db]
            [schema.core :as s]
            [versioned.types :refer [Map Model Request App Changelog Action Email]]))

(s/defn get-user :- (s/maybe Email)
  [request :- Request]
  (get-in request [:user :email]))

(s/defn updated-by :- Map
  [request :- Request]
  {:updated_by (get-user request)})

(s/defn created-by :- Map
  [request :- Request]
  {:created_by (get-user request)})

(s/defn save-changelog :- (s/maybe Changelog)
  [app :- App, request :- Request, model :- Model, action :- Action, doc :- Map]
  (let [errors (not-empty (model-errors doc))
        changes (if (= :update action) (model-changes model doc) nil)
        user (get-user request)
        changelog-spec (get-model app :changelog)
        changelog-doc {
          :action action
          :doc doc
          :changes changes
          :created_by user
          :created_at (d/now)}]
    (if-not errors
      (model-api/create app changelog-spec changelog-doc))))
