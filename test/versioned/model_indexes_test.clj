(ns versioned.model-indexes-test
  (:require [clojure.test :refer :all]
            [versioned.test-helper :as test-helper]
            [versioned.model-indexes :refer [ensure-indexes]]))

(use-fixtures :each test-helper/fixture)

(deftest ensure-indexes-does-not-allow-invalid-args
  (is (thrown-with-msg? RuntimeException
                        #"does not match schema"
                        (ensure-indexes {} {}))))
