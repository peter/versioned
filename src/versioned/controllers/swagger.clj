(ns versioned.controllers.swagger
  (require [versioned.model-attributes :refer [api-writable-schema
                                               api-readable-schema
                                               without-custom-keys]]
           [versioned.util.core :as u]))

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
                          "enum" (keys (:models app))
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
  (let [name (name (:type model))
        write-schema (str "#/definitions/" name "_write")
        routes (set (or (:routes model) []))
        list-key (str "/" name)
        list-paths (not-empty (u/compact {
          "get" (if (routes :list) {
            "tags" [name],
            "summary" (str "List " name),
            "parameters" [
              {"$ref" "#/parameters/auth"}
            ],
            "responses" {
              "200" {
                  "description" (str "List of " name)
              }
            }
          })
          "post" (if (routes :create) {
            "tags" [name],
            "summary" (str "Create " name),
            "parameters" [
              {"$ref" "#/parameters/auth"},
              {
                "name" "body",
                "in" "body",
                "required" true,
                "schema" {
                  "type" "object",
                  "properties" {
                    "data" {
                      "type" "object",
                      "properties" {
                        "attributes" {"$ref" write-schema}
                      },
                      "required" ["attributes"]
                    }
                  },
                  "required" ["data"]
                }
              }
            ]
            "responses" {
              "200" {
                  "description" "Success"
              },
              "422" {
                  "description" "Validation errors"
              }
            }
          })
        }))
        id-key (str "/" name "/{id}")
        id-paths (not-empty (u/compact {
          "get" (if (routes :get) {
            "tags" [name],
            "summary" (str "Get " name),
            "parameters" [
              {"$ref" "#/parameters/auth"},
              {"$ref" "#/parameters/id"}
            ],
            "responses" {
              "200" {
                  "description" "Success"
              }
            }
          })
          "put" (if (routes :update) {
            "tags" [name],
            "summary" (str "Update " name),
            "parameters" [
              {"$ref" "#/parameters/auth"},
              {"$ref" "#/parameters/id"},
              {
                "name" "body",
                "in" "body",
                "required" true,
                "schema" {
                  "type" "object",
                  "properties" {
                    "data" {
                      "type" "object",
                      "properties" {
                        "attributes" {"$ref" write-schema}
                      },
                      "required" ["attributes"]
                    }
                  },
                  "required" ["data"]
                }
              }
            ]
            "responses" {
              "200" {
                  "description" "Success"
              },
              "422" {
                  "description" "Validation errors"
              }
            }
          })
          "delete" (if (routes :delete) {
            "tags" [name],
            "summary" (str "Delete " name),
            "parameters" [
              {"$ref" "#/parameters/auth"},
              {"$ref" "#/parameters/id"}
            ],
            "responses" {
              "200" {
                  "description" "Success"
              }
            }
          })
        }))]
    (u/compact {
      list-key list-paths
      id-key id-paths
    })))


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
