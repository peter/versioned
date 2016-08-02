(ns versioned.routes
  (:require [versioned.util.core :as u]
            [versioned.router.core :refer [get-route-match]]
            [versioned.crud-api :as crud-api]))

; TODO: a cleaner syntax for specifying API endpoints and controller handlers
(defn- parse-handler [handler]
  (drop 1 (re-matches #"(.+)\/(api:)?(.+)" handler)))

(defn- lookup-var-handler [controller action]
  (let [path (str "versioned.controllers." controller "/" action)]
    (u/load-var path)))

; TODO: the API implementation here is a bit of a hack. Would be nice to find a cleaner/simpler solution
(defn- lookup-api-handler [models controller action]
  (let [crud-fn (u/load-var (str "versioned.crud-api/" action))
        model-name (keyword controller)
        model-spec (model-name models)
        api (crud-api/new-api :model-spec model-spec)]
    (partial crud-fn api)))

(defn lookup-handler-uncached [models route]
  (let [[controller api action] (parse-handler (:handler route))]
    (if api
      (lookup-api-handler models controller action)
      (lookup-var-handler controller action))))

(def lookup-handler (memoize lookup-handler-uncached))

(def crud-actions [:list :get :create :update :delete])

(defn route-requires-auth? [app request]
  (let [route-match (get-route-match app request)]
    (if route-match
      (get-in route-match [:route :swagger "x-auth-required"] true)
      false)))

(defn prefixed-path [api-prefix path spec]
  (let [prefix? (get spec "x-api-prefix" true)]
    (if prefix?
      (str api-prefix path)
      path)))

(defn routes-for-path [api-prefix [path methods]]
  (map (fn [[method spec]]
         {:methods #{(keyword method)}
          :path (prefixed-path api-prefix path spec)
          :swagger spec
          :handler (get spec "x-handler")})
        methods))

(defn routes [app]
  (let [paths (get-in app [:swagger :paths])
        api-prefix (get-in app [:config :api-prefix])]
    (flatten (map (partial routes-for-path api-prefix) paths))))

(defn routes-with-handlers [app]
  (map #(assoc % :handler (lookup-handler (:models app) %)) (routes app)))
