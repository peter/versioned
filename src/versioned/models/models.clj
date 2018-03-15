(ns versioned.models.models
  (:require [schema.core :as s]
            [versioned.types :refer [Schema]]
            [versioned.swagger.core :refer [init-swagger]]
            [versioned.routes :refer [init-routes]]
            [versioned.model-spec :refer [generate-spec]]
            [versioned.model-includes.content-base-model :refer [content-base-spec]]
            [versioned.swagger.core :as swagger]
            [versioned.model-validations :refer [with-model-errors]]
            [versioned.model-init :refer [get-model get-models init-models merge-schemas]]
            [versioned.util.schema :refer [validate-schema]]
            [versioned.util.json :as json]))

(def model-type :models)

(defn validate-schema-callback [doc options]
  (if-let [errors (s/check Schema (:schema doc))]
    (with-model-errors doc [{:type "invalid_schema" :message (str errors)}])
    doc))

(defn validate-swagger-callback [doc options]
  (let [model-type (keyword (:model_type doc))
        model (get-model (:app options) (keyword (:model_type doc)))]
    (if (and model-type model (:schema doc))
      (let [models (get-models (:app options))
            schema (get-in models [model-type :schema])
            updated-schema (merge-schemas (:schema doc) schema)
            updated-models (assoc-in models [(:type model) :schema] updated-schema)
            updated-app {:config (:config options) :models (atom updated-models)}
            errors (swagger/validate updated-app)]
        (with-model-errors doc errors))
      doc)))

(defn update-app-callback [doc options]
  (let [app (:app options)
        database (:database options)
        config (:config options)
        models (:models app)
        swagger (:swagger app)
        routes (:routes app)]
    (reset! models (init-models config database))
    (reset! swagger (init-swagger {:config config :models models}))
    (reset! routes (init-routes app))
    doc))

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
          :before [validate-schema-callback validate-swagger-callback]
          :after [update-app-callback]
        }
      }
      :indexes [
        {:fields [:model_type] :unique true}
      ]
    }))
