(ns versioned.routes
  (:require [versioned.util.core :as u]
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

(defn- crud-routes [model & {:keys [actions api-prefix]
                             :or {actions crud-actions}}]
  (vals (select-keys {
    :list {:methods #{:get} :path (str api-prefix "/" (name model)) :handler (str (name model) "/api:list")}
    :get {:methods #{:get} :path (str api-prefix "/" (name model) "/:id") :handler (str (name model) "/api:get")}
    :create {:methods #{:post} :path (str api-prefix "/" (name model)) :handler (str (name model) "/api:create")}
    :update {:methods #{:patch :put} :path (str api-prefix "/" (name model) "/:id") :handler (str (name model) "/api:update")}
    :delete {:methods #{:delete} :path (str api-prefix "/" (name model) "/:id") :handler (str (name model) "/api:delete")}
  } actions)))

(defn- model-routes [models api-prefix]
  (map #(crud-routes (:type %) :actions (:routes %) :api-prefix api-prefix) (filter :routes (vals models))))

(defn routes [app]
  (let [api-prefix (get-in app [:config :api-prefix])]
    (flatten [
      {:methods #{:get} :path "/" :handler "home/index"}
      {:methods #{:post} :path (str api-prefix "/login") :handler "sessions/create"}
      {:methods #{:post} :path (str api-prefix "/bulk_import") :handler "bulk-import/create"}
      (model-routes (:models app) api-prefix)
    ])))

(defn routes-with-handlers [app]
  (map #(assoc % :handler (lookup-handler (:models app) %)) (routes app)))
