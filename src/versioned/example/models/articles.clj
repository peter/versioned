(ns versioned.example.models.articles
  (:require [versioned.model-spec :refer [generate-spec]]))

(def model-type :articles)

(defn spec [config]
  (generate-spec
    {
    :type model-type
    :schema {
      :type "object"
      :properties {
        :_id {:type "string" :pattern "^[a-z0-9]{24}$" :x-meta {:api_writable false}}
        :title {:type "string"}
        :body {:type "string"}
      }
      :additionalProperties false
      :required [:title]
    }
    :routes [:get :list :create :update :delete]
  }))
