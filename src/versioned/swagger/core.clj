(ns versioned.swagger.core
  (require [versioned.util.core :as u]
           [versioned.model-attributes :refer [api-writable-schema
                                               api-readable-schema
                                               without-custom-keys]]
           [versioned.swagger.paths.api-docs :as api-docs-paths]
           [versioned.swagger.paths.login :as login-paths]
           [versioned.swagger.paths.import :as import-paths]
           [versioned.swagger.paths.model :as model-paths]
           [versioned.swagger.ref :refer [deep-resolve-ref]]))

(defn- paths [app]
  (let [endpoints [(api-docs-paths/swagger app)
                   (login-paths/swagger app)
                   (import-paths/swagger app)]
        models (map (partial model-paths/swagger app) (vals (:models app)))
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
          (vals (:models app))))

(defn- parameters [app]
    {
      :auth {
        :name "Authorization"
        :in "header"
        :required true
        :type "string"
      }
    })

(defn- version [app]
  (-> "project.clj" slurp read-string (nth 2)))

; NOTE: see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md
(defn swagger [app]
  (deep-resolve-ref {
      :swagger "2.0"
      :info {
          :title "Versioned API"
          :description "A REST CMS API based on MongoDB"
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
