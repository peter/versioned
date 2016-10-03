(ns versioned.model-init
  (:require [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.types :refer [Models Model ModelOrPath Config]]))

(s/defn load-model-spec :- Model
  [config :- Config
   spec :- ModelOrPath]
  (if (string? spec)
    (let [spec-fn (u/load-var spec)
          loaded-model (spec-fn config)]
      loaded-model)
    spec))

(s/defn init-models :- Models
  [config :- Config]
  (reduce (fn [models [type spec]]
            (assoc models type (load-model-spec config spec)))
          {}
          (:models config)))
