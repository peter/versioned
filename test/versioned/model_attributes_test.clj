(ns versioned.model-attributes-test
  (:require [clojure.test :refer :all]
            [versioned.model-attributes :refer [api-writable-attributes]]))

(deftest api-writable-attributes_selects-attributes-that-are-api_writable-according-to-the-schema
  (let [attributes {
          :title "The title"
          :version_number 100}
        schema {
          :type "object"
          :properties {
            :title {:type "string"}
            :version_number {:type "integer" :meta {:api_writable false}}
          }}]
    (is (= (api-writable-attributes schema attributes) {:title "The title"}))))
