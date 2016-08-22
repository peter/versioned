(ns versioned.test-helper
  (:require [clojure.spec.test :as stest]))

(defn setup []
  (stest/instrument))

(defn teardown [])

(defn fixture [f]
  (setup)
  (f)
  (teardown))
