(ns versioned.routes
  (:require [versioned.util.core :as u]
            [clojure.string :as str]
            [versioned.types :refer [crud-actions]]
            [versioned.model-init :refer [get-models]]
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

(defn prefixed-path [api-prefix path spec]
  (if (get spec :x-api-prefix true)
      (str api-prefix path)
      path))

(defn routes-for-path [api-prefix [path methods]]
  (map (fn [[method spec]]
         {:methods #{(keyword method)}
          :path (prefixed-path api-prefix path spec)
          :swagger spec
          :handler (:x-handler spec)})
        methods))

(defn routes [app]
  (let [swagger (deref (:swagger app))
        paths (:paths swagger)
        api-prefix (get-in app [:config :api-prefix])]
    (flatten (map (partial routes-for-path api-prefix) paths))))

(defn init-routes [app]
  (map #(assoc % :handler (lookup-handler (get-models app) %)) (routes app)))
