(ns versioned.model-init
  (:require [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.schema :refer [Models Config]]))

(defn load-model-spec [config spec]
  (if (string? spec)
    (let [spec-fn (u/load-var spec)
          loaded-spec (spec-fn config)]
      loaded-spec)
    spec))

(s/defn init-models :- Models
  [config :- Config]
  (reduce (fn [models [type spec]]
            (assoc models type (load-model-spec config spec)))
          {}
          (:models config)))
