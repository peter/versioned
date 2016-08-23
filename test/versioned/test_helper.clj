(ns versioned.test-helper
  (:require [clojure.spec.test :as stest]
            [schema.core :as schema]))

(defn setup []
  (stest/instrument)
  (schema/set-fn-validation! true))

(defn teardown [])

(defn fixture [f]
  (setup)
  (f)
  (teardown))
