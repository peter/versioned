(ns versioned.crud-api-attributes-test
  (:require [clojure.test :refer :all]
            [versioned.test-helper :as test-helper]
            [versioned.crud-api-attributes :refer [write-attributes]]))

(use-fixtures :each test-helper/fixture)

(deftest write-attributes-does-not-allow-invalid-args
  (is (thrown-with-msg? RuntimeException
                        #"does not match schema"
                        (write-attributes {} {}))))
