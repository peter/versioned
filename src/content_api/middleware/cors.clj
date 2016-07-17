(ns content-api.middleware.cors)

(def cors-headers {
  "access-control-allow-origin" "*"
  "access-control-expose-headers" ""
  "access-control-allow-credentials" "true"
  "access-control-allow-headers" "Authorization,Content-Type,Accept,Origin,User-Agent,DNT,Cache-Control,X-Mx-ReqToken,Keep-Alive,X-Requested-With,If-Modified-Since,X-CSRF-Token"
  "access-control-allow-methods" "GET,POST,PUT,PATCH,DELETE,OPTION"
})

(defn preflight?
  "Returns true if the request is a preflight request"
  [request]
  (= (request :request-method) :options))

(def preflight-response {
  :status 204
  :headers cors-headers})

(defn wrap-cors [handler]
  (fn [request]
    (if (preflight? request)
      preflight-response
      (let [response (handler request)]
        (update-in response [:headers] merge cors-headers)))))
