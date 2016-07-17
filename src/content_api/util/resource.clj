(ns content-api.util.resource
  (:refer-clojure :exclude [get])
  (:require [clojure.java.io :as io]))

(defn get [path]
  (slurp (io/resource "404.html")))
