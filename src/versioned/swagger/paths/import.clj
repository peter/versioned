(ns versioned.swagger.paths.import)

(defn swagger [app]
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
