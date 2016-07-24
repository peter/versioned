(ns versioned.util.json
  (:require [cheshire.core :as json]))

(defn parse [str]
  (json/parse-string str true))

(defn generate [data]
  (json/generate-string data {:pretty true}))
