(ns versioned.swagger.paths.model
  (:require [versioned.util.core :as u]))

(defn- data-schema [attributes-schema]
  {
    "type" "object",
    "properties" {
      "data" {
        "type" "object",
        "properties" {
          "attributes" {"$ref" attributes-schema}
        },
        "required" ["attributes"]
      }
    },
    "required" ["data"]
  })

(defn swagger [app model]
  (let [name (name (:type model))
        read-schema (str "#/definitions/" name "_read")
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
                "schema" {
                  "type" "object"
                  "properties" {
                    "data" {
                      "type" "array"
                      "items" {
                        "type" "object"
                        "properties" {
                          "attributes" {"$ref" read-schema}
                        }
                        "required" ["attributes"]
                      }
                    }
                  }
                  "required" ["data"]
                }
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
                "schema" (data-schema write-schema)
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
                "schema" (data-schema read-schema)
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
                "schema" (data-schema write-schema)
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
