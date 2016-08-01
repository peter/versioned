(ns versioned.swagger.paths.api-docs)

(defn swagger [app]
  {
    "/" {
        "get" {
            "tags" ["api docs"]
            "summary" "Swagger API Documentation HTML page"
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
          "responses" {
            "200" {
              "description" "Swagger JSON API specification"
            }
          }
      }
    }})
