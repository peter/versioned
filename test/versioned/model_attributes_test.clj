(ns versioned.model-attributes-test
  (:require [clojure.test :refer :all]
            [versioned.model-attributes :refer [api-writable-attributes without-custom-keys]]))

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

(deftest without-custom-keys-test
  (testing "can strip out the meta property from schemas"
    (let [schema {
            :type "object"
            :properties {
              :title {:type "string" :meta "foo"}
              :version_number {
                :type "array"
                :items {
                  :type "object"
                  :properties {
                    :title {:type "string" :meta "bar"}
                  }
                }
              }
            }
          }
          expected {
            :type "object"
            :properties {
              :title {:type "string"}
              :version_number {
                :type "array"
                :items {
                  :type "object"
                  :properties {
                    :title {:type "string"}
                  }
                }
              }
            }
          }]
      (is (= (without-custom-keys schema) expected)))))
