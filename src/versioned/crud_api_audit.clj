(ns versioned.crud-api-audit
  (:require [versioned.model-validations :refer [model-errors]]
            [versioned.util.date :as d]
            [versioned.model-api :as model-api]
            [versioned.model-changes :refer [model-changes]]
            [versioned.db-api :as db]
            [clojure.spec :as s]))

(s/fdef get-user
  :args (s/cat :request map?)
  :ret (s/nilable string?))

(defn- get-user [request]
  (get-in request [:user :email]))

(s/fdef updated-by
  :args (s/cat :request map?)
  :ret map?)

(defn updated-by [request]
  {:updated_by (get-user request)})

(s/fdef created-by
  :args (s/cat :request map?)
  :ret map?)

(defn created-by [request]
  {:created_by (get-user request)})

(s/def ::action #{:update :create :delete})
(s/fdef save-changelog
  :args (s/cat :app map? :request map? :model map? :action ::action :doc map?)
  :ret map?)

(defn save-changelog [app request model action doc]
  (let [errors (not-empty (model-errors doc))
        changes (if (= :update action) (model-changes model doc) nil)
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
