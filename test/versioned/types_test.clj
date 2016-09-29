(ns versioned.types-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [versioned.types :refer [Model
                                     ModelsConfig
                                     ScalarSchema
                                     ObjectSchema
                                     ArraySchema
                                     JsonSchema]]))

(deftest model-schema-validates-model-specs
  (testing "Model"
    (let [valid-basic-model {
            :type :my-model
            :schema {
              :type "object"
              :properties {:title {:type "string"}}
            }
          }
          valid-model (merge valid-basic-model {
            :relationships {
              :widgets {}
            }
            :routes [:get :list :create :update :delete]
          })
          empty-model {}
          invalid-routes-model (merge valid-model {:routes [:foobar]})]
        (is (s/validate Model valid-basic-model))
        (is (s/validate Model valid-model))
        (is (thrown-with-msg? RuntimeException #"does not match schema" (s/validate Model empty-model)))
        (is (thrown-with-msg? RuntimeException #"does not match schema"  (s/validate Model invalid-routes-model)))
      ))
  (testing "ModelsConfig"
    (is (s/validate ModelsConfig {:articles {:type :articles :schema {:type "object" :properties {:title {:type "string"}}}}}))
    (is (thrown-with-msg? RuntimeException #"does not match schema" (s/validate ModelsConfig {:articles {:type :foo :schema {:type "object"}}})))
    (is (thrown-with-msg? RuntimeException #"does not match schema" (s/validate ModelsConfig {:articles {:type :articles}})))
  )
  (testing "ScalarSchema"
    (is (s/validate ScalarSchema {:type "string"}))
    (is (s/validate ScalarSchema {:type "number"}))
    (is (s/validate ScalarSchema {:type "integer"}))
    (is (s/validate ScalarSchema {:type "boolean"}))
    (is (s/validate ScalarSchema {:type ["string" "null"]}))
  )
  (testing "ObjectSchema"
    (is (s/validate ObjectSchema {
      :type "object"
      :properties {
        :title {:type "string"}
        :admin {:type "boolean"}
      }
      :required [:title]
      :additionalProperties false
    }))
  )
  (testing "ArraySchema"
    (is (s/validate ArraySchema {
      :type "array"
      :items {
        :type "object"
        :properties {
          :title {:type "string"}
          :admin {:type "boolean"}
        }
        :required [:title]
        :additionalProperties false
      }
    }))
    (is (s/validate ArraySchema {
      :type "array"
      :items {
        :type "string"
      }
    }))
  )
  (testing "JsonSchema"
    (is (s/validate JsonSchema {:type "string"}))
    (is (s/validate JsonSchema {:type "number"}))
    (is (s/validate JsonSchema {:type "integer"}))
    (is (s/validate JsonSchema {:type "boolean"}))
    (is (s/validate JsonSchema {:type ["string" "null"]}))
    (is (s/validate JsonSchema {
      :type "object"
      :properties {
        :title {:type "string"}
        :admin {:type "boolean"}
      }
      :required [:title]
      :additionalProperties false
    }))
    (is (s/validate JsonSchema {
      :type "object"
      :properties {
        :title {
          :type "object"
          :properties {
            :se {:type "string"}
          }
        }
        :admin {:type "boolean"}
      }
      :required [:title]
      :additionalProperties false
    }))
    (is (s/validate JsonSchema {
      :type "array"
      :items {
        :type "object"
        :properties {
          :title {:type "string"}
          :admin {:type "boolean"}
        }
        :required [:title]
        :additionalProperties false
      }
    }))
  )
)
