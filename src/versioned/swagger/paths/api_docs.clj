(ns versioned.swagger.paths.api-docs)

(defn swagger [app]
  {
    "/" {
        "get" {
            "tags" ["api docs"]
            "summary" "Swagger API Documentation HTML page"
            "x-handler" "home/index"
            "x-api-prefix" false
            "x-auth-required" false
            "responses" {
              "301" {
                "description" "Redirect to HTML page with Swagger API docs"
              }
            }
        }
      }
    "/swagger.json" {
      "get" {
          "tags" ["api docs"]
          "summary" "Swagger API JSON specification"
          "x-handler" "swagger/index"
          "x-auth-required" false
          "responses" {
            "200" {
              "description" "Swagger JSON API specification"
            }
          }
      }
    }})
