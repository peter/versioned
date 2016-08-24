(ns versioned.model-spec-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [versioned.model-spec :as model-spec]))

(deftest model-schema-validates-model-specs
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
      (is (s/validate model-spec/Model valid-basic-model))
      (is (s/validate model-spec/Model valid-model))
      (is (thrown-with-msg? RuntimeException #"does not match schema" (s/validate model-spec/Model empty-model)))
      (is (thrown-with-msg? RuntimeException #"does not match schema"  (s/validate model-spec/Model invalid-routes-model)))
    ))

(deftest generate-spec-deep-merges-schema-callbacks-indexes-for-specs
  (let [spec1 {
          :type "spec1"
          :schema {
            :properties {
              :spec1 {:type "string"}
            }
            :required [:spec1]
          }
          :callbacks {
            :create {
              :before [:spec1]
            }
          }
          :indexes [{:fields [:spec1]}]
        }
        spec2 {
          :type "spec2"
          :schema {
            :properties {
              :spec2 {:type "integer"}
            }
            :required [:spec2]
          }
          :callbacks {
            :create {
              :before [:spec2]
            }
          }
          :indexes [{:fields [:spec2]}]
        }
        expect {
          :type "spec2"
          :schema {
            :properties {
              :spec1 {:type "string"}
              :spec2 {:type "integer"}
            }
            :required [:spec1 :spec2]
          }
          :callbacks {
            :create {
              :before [:spec1 :spec2]
            }
          }
          :indexes [{:fields [:spec1]} {:fields [:spec2]}]
        }]
  (is (= (model-spec/generate-spec spec1 spec2) expect))))
