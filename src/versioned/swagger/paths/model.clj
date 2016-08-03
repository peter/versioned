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

(defn model-name [model]
  (name (:type model)))

(defn add-route? [model route]
  (let [routes (set (or (:routes model) []))]
    (routes route)))

(defn read-schema-ref [model]
  (str "#/definitions/" (model-name model) "_read"))

(defn read-schema [model]
  (data-schema (read-schema-ref model)))

(defn write-schema-ref [model]
  (str "#/definitions/" (model-name model) "_write"))

(defn write-schema [model]
  (data-schema (write-schema-ref model)))

(defn list-paths [app model]
  (let [name (model-name model)]
    (not-empty (u/compact {
        "get" (if (add-route? model :list) {
          "tags" [name],
          "summary" (str "List " name),
          "x-handler" (str name "/api:list")
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
                        "attributes" {"$ref" (read-schema-ref model)}
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
        "post" (if (add-route? model :create) {
          "tags" [name],
          "summary" (str "Create " name),
          "x-handler" (str name "/api:create")
          "parameters" [
            {"$ref" "#/parameters/auth"},
            {
              "name" "body",
              "in" "body",
              "required" true,
              "schema" (write-schema model)
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
      }))))

(defn id-paths [app model]
  (let [name (model-name model)]
    (not-empty (u/compact {
        "get" (if (add-route? model :get) {
          "tags" [name],
          "summary" (str "Get " name),
          "x-handler" (str name "/api:get")
          "parameters" [
            {"$ref" "#/parameters/auth"},
            {"$ref" "#/parameters/id"}
          ],
          "responses" {
            "200" {
              "description" "Success"
              "schema" (read-schema model)
            }
          }
        })
        "put" (if (add-route? model :update) {
          "tags" [name],
          "summary" (str "Update " name),
          "x-handler" (str name "/api:update")
          "parameters" [
            {"$ref" "#/parameters/auth"},
            {"$ref" "#/parameters/id"},
            {
              "name" "body",
              "in" "body",
              "required" true,
              "schema" (write-schema model)
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
        "delete" (if (add-route? model :delete) {
          "tags" [name],
          "summary" (str "Delete " name),
          "x-handler" (str name "/api:delete")
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
      }))))

(defn swagger [app model]
  (let [name (model-name model)
        list-key (str "/" name)
        id-key (str "/" name "/{id}")]
    (u/compact {
      list-key (list-paths app model)
      id-key (id-paths app model)
    })))
