(ns versioned.model-init-test
  (:require [clojure.test :refer :all]
            [versioned.test-helper :as test-helper]
            [versioned.model-init :refer [init-models]]))

(use-fixtures :each test-helper/fixture)

(deftest model-init
  (testing "init-models"
    (testing "raises schema exception if passed invalid models arg"
      (is (thrown-with-msg? RuntimeException #"does not match schema" (init-models nil)))
      (is (thrown-with-msg? RuntimeException #"does not match schema" (init-models [])))
      (is (thrown-with-msg? RuntimeException #"does not match schema" (init-models {:models {:foo "this model path is invalid"}})))
      (is (thrown-with-msg? RuntimeException #"does not match schema" (init-models {:models {:foo {:type :bar :schema {:type "object"}}}})))
      (is (thrown-with-msg? RuntimeException #"does not match schema" (init-models {:models {:bar {:type :foo}}})))
    )
    (testing "can be passed models map where values are paths to spec functions (strings)"
      (is (init-models {:models {:foo "versioned.example.models.sections/spec"}}))
    )
    (testing "can be passed valid models map where values are model specs (maps)"
      (is (init-models {:models {:foo {:type :foo :schema {:type "object"}}}}))
    )
  )
)
