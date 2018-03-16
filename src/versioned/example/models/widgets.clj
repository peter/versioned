(ns versioned.example.models.widgets
  (:require [versioned.example.models.shared :refer [set-sites-callback sites-schema translated-attribute]]
            [versioned.model-spec :refer [generate-spec]]
            [versioned.example.searchable-model :refer [searchable-model-spec]]
            [versioned.base-models.published :as base-model]))

(def model-type :widgets)

(defn spec [config]
  (let [locales (:locales config)]
    (generate-spec
      (base-model/spec model-type)
      (searchable-model-spec)
      {
      :type model-type
      :schema {
        :type "object"
        :x-meta {
          :admin_properties [:title :description]
        }
        :properties {
          :title (translated-attribute locales)
          :description (translated-attribute locales)
          :sites (sites-schema config)
          :widgets_type {:type "string"}
          :legacy {:type "object"}
        }
        :additionalProperties false
        :required [:title]
      }
      :callbacks {
        :save {
          :before [set-sites-callback]
        }
      }
      :indexes [
        {:fields [:title.se] :unique true}
      ]
    })))
