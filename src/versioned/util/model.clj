(ns versioned.util.model
  (:require [versioned.util.core :as u]))

(defn get-models [app]
  (deref (:models app)))

(defn get-model [app model]
  (get-in (get-models app) [model]))

(defn get-in-model [app path]
  (get-in (get-models app) path))
