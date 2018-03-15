(ns versioned.models.models
  (:require [versioned.model-spec :refer [generate-spec merge-schemas]]
            [versioned.model-includes.content-base-model :refer [content-base-spec]]
            [versioned.swagger.core :as swagger]
            [versioned.model-validations :refer [with-model-errors]]
            [versioned.model-init :refer [get-model get-models set-models]]
            [versioned.util.schema :refer [validate-schema]]
            [versioned.util.json :as json]))

(def model-type :models)

(defn validate-swagger-callback [doc options]
  (let [model-type (keyword (:model_type doc))
        model (get-model (:app options) (keyword (:model_type doc)))]
    ; TODO: validate using versioned.types/Schema?
    ; TODO: need a try/catch here?
    (if (and model-type model)
      (let [models (get-models (:app options))
            schema (get-in models [model-type :schema])
            updated-schema (merge-schemas (:schema doc) schema)
            updated-models (assoc-in models [(:type model) :schema] updated-schema)
            swagger-spec (swagger/swagger {:config (:config options) :models updated-models})
            errors (validate-schema (swagger/schema) swagger-spec)]
        (with-model-errors doc errors))
      doc)))

(defn update-app-callback [doc options]
  ; TODO: update model schema in app
  ; TODO: update swagger in app
  doc)

; TODO: when app boots it needs to initialize models with schemas in the database

(defn spec [config]
  (generate-spec
      (content-base-spec model-type)
      {
      :type model-type
      :schema {
        :type "object"
        :x-meta {
          :admin_properties [:model_type :schema]
          :require-read-auth true
        }
        :properties {
          :model_type {:type "string"}
          :schema {
            :type "object"
          }
        }
        :additionalProperties false
        :required [:model_type :schema]
      }
      :callbacks {
        :save {
          :before [validate-swagger-callback]
          :after [update-app-callback]
        }
      }
      :indexes [
        {:fields [:model_type] :unique true}
      ]
    }))
