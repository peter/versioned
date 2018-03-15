(ns versioned.swagger.core
  (:require [versioned.util.core :as u]
            [versioned.util.json :as json]
            [versioned.util.resource :as resource]
            [versioned.util.schema :refer [validate-schema]]
            [versioned.model-attributes :refer [api-writable-schema
                                                api-readable-schema
                                                without-custom-keys]]
            [versioned.swagger.paths.api-docs :as api-docs-paths]
            [versioned.swagger.paths.login :as login-paths]
            [versioned.model-init :refer [get-models]]
            [versioned.swagger.paths.import :as import-paths]
            [versioned.swagger.paths.model :as model-paths]
            [versioned.swagger.ref :refer [deep-resolve-ref]]))

(defn schema []
  (json/parse (resource/get "swagger-2.0-schema.json")))

(defn- paths [app]
  (let [endpoints [(api-docs-paths/swagger app)
                   (login-paths/swagger app)
                   (import-paths/swagger app)]
        models (map (partial model-paths/swagger app) (vals (get-models app)))
        all (concat endpoints models)]
    (apply merge all)))

(defn- definitions [app]
  (reduce (fn [result model]
            (let [write-key (keyword (str (name (:type model)) "_write"))
                  write-schema (without-custom-keys (api-writable-schema (:schema model)))
                  read-key (keyword (str (name (:type model)) "_read"))
                  read-schema (without-custom-keys (api-readable-schema (:schema model)))]
              (assoc result write-key write-schema
                            read-key read-schema)))
          {}
          (vals (get-models app))))

(defn- parameters [app]
    {
      :auth {
        :name "Authorization"
        :description "User auth header on the format\"Bearer {token}\""
        :in "header"
        :required true
        :type "string"
      }
    })

(defn- version [app]
  (-> "project.clj" slurp read-string (nth 2)))

; NOTE: see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md
(defn init-swagger [app]
  (deep-resolve-ref {
      :swagger "2.0"
      :info {
          :title (get-in app [:config :title])
          :description (get-in app [:config :description])
          :version (version app)
      }
      :basePath (get-in app [:config :api-prefix])
      :produces [
          "application/json"
      ]
      :paths (paths app)
      :definitions (definitions app)
      :parameters (parameters app)
  }))

(defn validate [app]
  (validate-schema (schema) (init-swagger app)))
