(ns versioned.crud-api-audit
  (:require [versioned.model-validations :refer [model-errors]]
            [versioned.util.date :as d]
            [versioned.model-api :as model-api]
            [versioned.model-changes :refer [model-changes]]
            [versioned.db-api :as db]))

(defn- get-user [request]
  (get-in request [:user :email]))

(defn updated-by [request]
  {:updated_by (get-user request)})

(defn created-by [request]
  {:created_by (get-user request)})

(defn save-changelog [app request model-spec action doc]
  (let [errors (not-empty (model-errors doc))
        changes (if (= :update action) (model-changes model-spec doc) nil)
        user (get-user request)
        changelog-spec (get-in app [:models :changelog])
        changelog-doc {
          :action action
          :doc doc
          :changes changes
          :created_by user
          :created_at (d/now)}]
    (if-not errors
      (model-api/create app changelog-spec changelog-doc))))
