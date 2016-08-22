(ns versioned.model-validations-test
  (:require [clojure.test :refer :all]
            [versioned.model-validations :refer [validate-model-schema]]))

(deftest validate-model-schema-return-nil-if-there-are-no-schema-errors
  (is (= (validate-model-schema {:type "object" :properties {:title {:type "string"}} :required [:title]} {:title "The title"})
         nil)))

(deftest validate-model-schema-can-return-the-required-schema-error
  (is (= (map :keyword (validate-model-schema {:type "object" :properties {:title {:type "string"}} :required [:title]} {}))
         ["required"])))
