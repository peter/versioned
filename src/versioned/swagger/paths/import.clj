(ns versioned.swagger.paths.import)

(defn swagger [app]
  {
    "/import_initial" {
        :post {
            :tags ["import"]
            :summary "Initial bulk import"
            :x-handler "import-initial/create"
            :description "Import data, i.e. from another CMS"
            :parameters [
                {"$ref" "#/parameters/auth"}
                {
                    :name "body"
                    :in "body"
                    :required true
                    :schema {
                      :type "object"
                      :properties {
                        :model {
                          :enum (keys (:models app))
                        }
                        :data {
                          :type "array"
                          :items {
                            :type "object"
                          }
                        }
                      }
                      :required ["model" "data"]
                    }
                }
            ]
            :responses {
                "200" {
                    :description "Successful import"
                }
                "422" {
                    :description "Import errors"
                }
            }
        }
    }

    "/import_sync/delete" {
        :post {
            :tags ["import"]
            :summary "Sync deletes of bulk imported docs"
            :x-handler "import-sync/delete"
            :description "Delete all docs with ids that are not in set of given ids"
            :parameters [
                {"$ref" "#/parameters/auth"}
                {
                    :name "body"
                    :in "body"
                    :required true
                    :schema {
                      :type "object"
                      :properties {
                        :model {
                          :enum (keys (:models app))
                        }
                        :ids {
                          :type "array"
                          :items {
                            :type ["string", "integer"]
                          }
                        }
                        :id_attribute {
                          :type "string"
                        }
                      }
                      :required ["model" "ids" "id_attribute"]
                    }
                }
            ]
            :responses {
                "200" {
                    :description "Successful sync"
                }
                "422" {
                    :description "Sync errors"
                }
            }
        }
    }

    "/import_sync/upsert" {
        :post {
            :tags ["import"]
            :summary "Sync updates/inserts of bulk imported docs"
            :x-handler "import-sync/upsert"
            :description "Insert all docs that are new and update existing ones"
            :parameters [
                {"$ref" "#/parameters/auth"}
                {
                    :name "body"
                    :in "body"
                    :required true
                    :schema {
                      :type "object"
                      :properties {
                        :model {
                          :enum (keys (:models app))
                        }
                        :data {
                          :type "array"
                          :items {
                            :type "object"
                          }
                        }
                        :id_attribute {
                          :type "string"
                        }
                      }
                      :required ["model" "data" "id_attribute"]
                    }
                }
            ]
            :responses {
                "200" {
                    :description "Successful sync"
                }
                "422" {
                    :description "Sync errors"
                }
            }
        }
    }
  })
