(ns content-api.util.auth
  (:require [content-api.util.digest :as digest]
            [clojure.string :as str]))

(def header-name "Authorization")

(defn header [token]
  {header-name (str "Bearer " token)})

(defn parse-token [headers]
  (last (str/split (get headers (str/lower-case header-name) "") #" ")))

(defn generate-token []
  (digest/generate))
