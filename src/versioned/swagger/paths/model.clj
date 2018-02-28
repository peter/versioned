(ns versioned.swagger.paths.model
  (:require [versioned.util.core :as u]
            [versioned.crud-api-query :refer [query-pattern]]
            [versioned.model-support :refer [id-attribute]]))

(defn- data-schema [attributes-schema]
  {
    :type "object",
    :properties {
      :data {
        :type "object",
        :properties {
          :attributes {"$ref" attributes-schema}
        },
        :required ["attributes"]
      }
    },
    :required ["data"]
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

(defn id-parameter [model]
  (let [id-attr (id-attribute model)
        id-schema (dissoc (get-in model [:schema :properties id-attr] {}) :x-meta)]
    (merge id-schema {
      :name "id"
      :in "path"
      :required true
    })))

(defn list-paths [app model]
  (let [name (model-name model)]
    (not-empty (u/compact {
        :get (if (add-route? model :list) {
          :tags [name],
          :summary (str "List " name),
          :x-model (:type model)
          :x-handler (str name "/api:list")
          :parameters [
            {"$ref" "#/parameters/auth"}
            {
              :name "sort",
              :description "Which fields to sort by, comma separated. Prefix with - (minus) for descending. Default sort is by id desc"
              :in "query",
              :required false,
              :type "string"
            }
            {
              :name "page",
              :description "Which page to fetch, defaults to 1 (for pagination)"
              :in "query",
              :required false,
              :type "integer",
              :minimum 1
            }
            {
              :name "per-page",
              :description "Number of documents to fetch (for pagination)"
              :in "query",
              :required false,
              :type "integer",
              :minimum 1
            }
            {
              :name "q",
              :description "Query - filter out which documents are returned, format is separator:key:value where separator is optional and defaults to comma (,)"
              :in "query",
              :required false,
              :type "array"
              :items {
                :type "string"
                :pattern (str query-pattern)
              }
            },
            {
              :name "published",
              :description "Whether to only fetch published docs"
              :in "query",
              :required false,
              :type "boolean"
            }
          ],
          :responses {
            "200" {
              :description (str "List of " name)
              :schema {
                :type "object"
                :properties {
                  :data {
                    :type "array"
                    :items {
                      :type "object"
                      :properties {
                        :attributes {"$ref" (read-schema-ref model)}
                      }
                      :required ["attributes"]
                    }
                  }
                }
                :required ["data"]
              }
            }
          }
        })
        :post (if (add-route? model :create) {
          :tags [name],
          :summary (str "Create " name),
          :x-model (:type model)
          :x-handler (str name "/api:create")
          :parameters [
            {"$ref" "#/parameters/auth"},
            {
              :name "body",
              :in "body",
              :required true,
              :schema (write-schema model)
            }
          ]
          :responses {
            "200" {
                :description "Success"
            },
            "422" {
                :description "Validation errors"
            }
          }
        })
      }))))

(defn id-paths [app model]
  (let [name (model-name model)]
    (not-empty (u/compact {
        :get (if (add-route? model :get) {
          :tags [name],
          :summary (str "Get " name),
          :x-model (:type model)
          :x-handler (str name "/api:get")
          :parameters [
            {"$ref" "#/parameters/auth"},
            (id-parameter model)
            {
              :name "relationships",
              :description "Whether to fetch relationships for the document"
              :in "query",
              :required false,
              :type "boolean",
            }
            {
              :name "version",
              :description "Which version of the document to fetch, defaults to the latest version"
              :in "query",
              :required false,
              :type "integer",
              :minimum 1
            }
            {
              :name "published",
              :description "Whether to only fetch published version of document and its relationships"
              :in "query",
              :required false,
              :type "boolean",
            }
          ],
          :responses {
            "200" {
              :description "Success"
              :schema (read-schema model)
            }
          }
        })
        :put (if (add-route? model :update) {
          :tags [name],
          :summary (str "Update " name),
          :x-model (:type model)
          :x-handler (str name "/api:update")
          :parameters [
            {"$ref" "#/parameters/auth"},
            (id-parameter model)
            {
              :name "body",
              :in "body",
              :required true,
              :schema (write-schema model)
            }
          ]
          :responses {
            "200" {
                :description "Success"
            },
            "422" {
                :description "Validation errors"
            }
          }
        })
        :delete (if (add-route? model :delete) {
          :tags [name],
          :summary (str "Delete " name),
          :x-model (:type model)
          :x-handler (str name "/api:delete")
          :parameters [
            {"$ref" "#/parameters/auth"},
            (id-parameter model)
          ],
          :responses {
            "200" {
                :description "Success"
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
