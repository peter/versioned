(ns versioned.types-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [versioned.types :refer [Model ModelsConfig]]))

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
    (is (s/validate ModelsConfig {:articles {:type :articles :schema {:type "object"}}}))
    (is (thrown-with-msg? RuntimeException #"does not match schema" (s/validate ModelsConfig {:articles {:type :foo :schema {:type "object"}}})))
    (is (thrown-with-msg? RuntimeException #"does not match schema" (s/validate ModelsConfig {:articles {:type :articles}})))
  )
)
