(ns versioned.models.changelog
  (:require [versioned.model-spec :refer [generate-spec]]
            [versioned.model-includes.id-model :refer [id-spec]]))

(defn spec [config]
  (generate-spec
    (id-spec)
    {
      :type :changelog
      :schema {
        :type "object"
        :properties {
          :action {:enum ["create" "update" "delete"]}
          :doc {:type "object"}
          :changes {:type "object"}
          :created_by {:type "string" :format "email"}
          :created_at {:type "string" :format "date-time"}
        }
        :additionalProperties false
        :required [:action :doc :created_by :created_at]
      }
      :routes [:list :get]
    }))
