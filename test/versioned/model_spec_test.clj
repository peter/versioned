(ns versioned.model-spec-test
  (:require [clojure.test :refer :all]
            [versioned.model-spec :as model-spec]))

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
