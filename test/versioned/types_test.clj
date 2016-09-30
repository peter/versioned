(ns versioned.types-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [versioned.stubs :as stubs]
            [versioned.types :refer [Model
                                     App
                                     ModelsConfig
                                     Schema]]))

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
  (testing "App"
    (is (s/validate App stubs/app)))
  (testing "Schema"
    (is (s/validate Schema {:type "string"}))
    (is (s/validate Schema {:type "number"}))
    (is (s/validate Schema {:type "integer"}))
    (is (s/validate Schema {:type "boolean"}))
    (is (s/validate Schema {:type ["string" "null"]}))
    (is (s/validate Schema {
      :type "object"
      :properties {
        :title {:type "string"}
        :admin {:type "boolean"}
      }
      :required [:title]
      :additionalProperties false
    }))
    (is (s/validate Schema {
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
    (is (s/validate Schema {
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
    (is (not-empty ((s/checker Schema) {
      :type "object"
      :properties {
        :title {:type "string" :meta {:foo (fn [])}} ; Function is not a SchemaValue
      }})))
  )
)
