(ns content-api.example-models.pages
  (:require [content-api.example-models.shared :refer [set-sites-callback sites-schema]]
            [content-api.model-attributes :refer [translated-attribute]]
            [content-api.model-spec :refer [generate-spec]]
            [content-api.model-includes.content-base-model :refer [content-base-spec]]))

(def model-type :pages)

(defn spec [config]
  (let [locales (:locales config)]
    (generate-spec
      (content-base-spec model-type)
      {
      :type model-type
      :schema {
        :type "object"
        :properties {
          :title (translated-attribute locales)
          :description (translated-attribute locales)
          :slug (translated-attribute locales)
          :sites (sites-schema config)
          :widgets_ids {
            :type "array"
            :items {
              :type "integer"
            }
          }
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
      :relationships {
        :widgets {}
      }
      :indexes [
        ;{:fields [:title] :unique true}
      ]
    })))
