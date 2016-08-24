(ns versioned.schema
  (:require [schema.core :as s]))

(def Map {s/Keyword s/Any})
(def Nil (s/pred nil? 'nil?))
(def Function (s/pred fn? 'fn?))

(def Request Map)

(def Model Map)

(def Route Map)

(def Handler Function)

(def App {
  :config {s/Keyword s/Any}
  :models {s/Keyword Model}
  :swagger Map
  :routes [Route]
  s/Keyword s/Any
})
