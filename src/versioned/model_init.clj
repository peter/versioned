(ns versioned.model-init
  (:require [versioned.util.core :as u]))

(defn load-model-spec [config spec]
  (if (string? spec)
    (let [spec-fn (u/load-var spec)
          loaded-spec (spec-fn config)]
      loaded-spec)
    spec))

(defn init-models [config]
  (reduce (fn [models [type spec]]
            (assoc models type (load-model-spec config spec)))
          {}
          (:models config)))
