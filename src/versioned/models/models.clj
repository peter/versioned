(ns versioned.models.models
  (:require [schema.core :as s]
            [versioned.types :refer [Schema]]
            [versioned.swagger.core :refer [init-swagger]]
            [versioned.routes :refer [init-routes]]
            [versioned.model-spec :refer [generate-spec]]
            [versioned.base-models.content :as base-model]
            [versioned.swagger.core :as swagger]
            [versioned.model-validations :refer [with-model-errors]]
            [versioned.util.schema :refer [merge-schemas]]
            [versioned.util.model :refer [get-model get-models]]
            [versioned.model-init :refer [init-models]]
            [versioned.util.schema :refer [validate-schema]]))

(def model-type :models)

(defn validate-base-model [doc options]
  (let [model-type (keyword (:model_type doc))
        model (get-model (:app options) (keyword (:model_type doc)))
        source-path (get-in model [:schema :x-meta :source_path])
        message (str "You cannot select base model for model with source path " source-path)]
    (if (and source-path (:base_model doc))
      (with-model-errors doc [{:type "base_model" :message message}])
      doc)))

(defn validate-schema-callback [doc options]
  (if-let [errors (and (:schema doc)
                       (s/check Schema (:schema doc)))]
    (do
      (println "models/validate-schema-callback errors=" errors)
      (with-model-errors doc [{:type "json_schema" :message "Not a valid JSON schema"}]))
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

(defn spec [config]
  (generate-spec
      (base-model/spec model-type)
      {
      :type model-type
      :schema {
        :type "object"
        :x-meta {
          :admin_properties [:model_type :base_model :schema]
          :require-read-auth true
        }
        :properties {
          :model_type {:type "string"}
          :base_model {:enum ["content" "published"] :x-meta {:api_update false}}
          :schema {
            :type "object"
          }
        }
        :additionalProperties false
        :required [:model_type :schema]
      }
      :callbacks {
        :save {
          :before [validate-base-model validate-schema-callback validate-swagger-callback]
          :after [update-app-callback]
        }
        :delete {
          :after [update-app-callback]
        }
      }
      :indexes [
        {:fields [:model_type] :unique true}
      ]
    }))
