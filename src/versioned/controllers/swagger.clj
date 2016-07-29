(ns versioned.controllers.swagger
  (require [versioned.model-attributes :refer [api-writable-schema api-readable-schema without-custom-keys]]))

(defn login-paths [app]
  {
    "/login" {
      "post" {
        "tags" ["login"],
        "summary" "Login",
        "description" "Log in user with email/password and get token",
        "parameters" [
            {
                "name" "body",
                "in" "body",
                "required" true,
                "schema" {
                  "type" "object",
                  "properties" {
                    "email" {"type" "string"},
                    "password" {"type" "string"}
                  },
                  "required" ["email", "password"]
                }
            }
        ],
        "responses" {
            "200" {
                "description" "Successful login"
            },
            "401" {
                "description" "Failed login"
            }
        }
      }
    }
  })

(defn import-paths [app]
  {
    "/bulk_import" {
        "post" {
            "tags" ["import"]
            "summary" "Bulk Import"
            "description" "Import data, i.e. from another CMS"
            "parameters" [
                {"$ref" "#/parameters/auth"}
                {
                    "name" "body"
                    "in" "body"
                    "required" true
                    "schema" {
                      "type" "object"
                      "properties" {
                        "model" {
                          "enum" ["users" "sections" "pages" "widgets"]
                        }
                        "data" {
                          "type" "array"
                          "items" {
                            "type" "object"
                          }
                        }
                      }
                      "required" ["model" "data"]
                    }
                }
            ]
            "responses" {
                "200" {
                    "description" "Successful import"
                }
                "422" {
                    "description" "Import errors"
                }
            }
        }
    }
  })

(defn model-paths [app model]
  {})

(defn paths [app]
  (let [endpoints [(login-paths app) (import-paths app)]
        models (map (partial model-paths app) (vals (:models app)))
        all (concat endpoints models)]
    (apply merge all)))

(defn definitions [app]
  (reduce (fn [result model]
            (let [write-key (str (name (:type model)) "_write")
                  write-schema (without-custom-keys (api-writable-schema (:schema model)))
                  read-key (str (name (:type model)) "_read")
                  read-schema (without-custom-keys (api-readable-schema (:schema model)))]
              (assoc result write-key write-schema
                            read-key read-schema)))
          {}
          (vals (:models app))))

(defn parameters [app]
    {
      :auth {
        :name "Authorization"
        :in "header"
        :required true
        :type "string"
      }
      :id {
        :name "id"
        :in "path"
        :required true
        :type "integer"
      }
    })

(defn swagger [app]
  {
      :swagger "2.0"
      :info {
          :title "Versioned API"
          :description "A REST CMS API based on MongoDB"
          :version "0.2.0"
      }
      :host "versioned.herokuapp.com"
      :schemes [
          "https"
      ],
      :basePath "/v1"
      :produces [
          "application/json"
      ]
      :paths (paths app)
      :definitions (definitions app)
      :parameters (parameters app)
  })

(defn index [app request]
  {:body (swagger app) :status 200})
