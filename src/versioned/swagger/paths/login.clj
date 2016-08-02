(ns versioned.swagger.paths.login)

(defn swagger [app]
  {
    "/login" {
      "post" {
        "tags" ["login"],
        "summary" "Login",
        "x-auth-required" false
        "x-handler" "sessions/create"
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
