(ns content-api.model-init
  (:require [content-api.util.core :as u]))

(defn init-models [config]
  (reduce (fn [models [type spec-path]]
            (let [spec-fn (u/load-var spec-path)
                  spec (spec-fn config)]
              (assoc models type spec)))
          {}
          (:models config)))
