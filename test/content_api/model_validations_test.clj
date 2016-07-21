(ns content-api.model-validations-test
  (:use midje.sweet)
  (:require [content-api.model-validations :refer [validate-model-schema]]))

(fact "validate-model-schema: return nil if there are no schema errors"
  (validate-model-schema {:type "object" :properties {:title {:type "string"}} :required [:title]} {:title "The title"})
    => nil)

(fact "validate-model-schema: can return the required schema error"
  (map :keyword (validate-model-schema {:type "object" :properties {:title {:type "string"}} :required [:title]} {}))
    => ["required"])
