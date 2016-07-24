(ns versioned.model-init
  (:require [versioned.util.core :as u]))

(defn init-models [config]
  (reduce (fn [models [type spec-path]]
            (let [spec-fn (u/load-var spec-path)
                  spec (spec-fn config)]
              (assoc models type spec)))
          {}
          (:models config)))
