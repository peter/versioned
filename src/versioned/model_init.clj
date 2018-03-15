(ns versioned.model-init
  (:require [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.types :refer [ModelsRef Model ModelOrPath Config]]))

(defn ref-models [models]
  (atom models))

(defn deref-models [models]
  (deref models))

(defn get-models [app]
  (deref-models (:models app)))

(defn set-models [app models]
  (reset! (:models app) models))

(defn get-model [app model]
  (get-in (get-models app) [model]))

(defn get-in-model [app path]
  (get-in (get-models app) path))

(s/defn load-model-spec :- Model
  [config :- Config
   spec :- ModelOrPath]
  (if (string? spec)
    (let [spec-fn (u/load-var spec)
          loaded-model (spec-fn config)]
      loaded-model)
    spec))

(s/defn init-models :- ModelsRef
  [config :- Config]
  (ref-models (reduce (fn [models [type spec]]
                (assoc models type (load-model-spec config spec)))
                {}
                (:models config))))
