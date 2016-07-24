(ns versioned.model-attributes-test
  (:use midje.sweet)
  (:require [versioned.model-attributes :refer [api-writable-attributes]]))

(fact "api-writable-attributes: selects attributes that are api_writable according to the schema"
  (let [attributes {
          :title "The title"
          :version_number 100}
        schema {
          :type "object"
          :properties {
            :title {:type "string"}
            :version_number {:type "integer" :meta {:api_writable false}}
          }}]
    (api-writable-attributes schema attributes) => {:title "The title"}))
