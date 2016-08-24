(ns versioned.test-helper
  (:require [schema.core :as schema]))

(defn setup []
  (schema/set-fn-validation! true))

(defn teardown [])

(defn fixture [f]
  (setup)
  (f)
  (teardown))
