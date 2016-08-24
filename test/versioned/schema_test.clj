(ns versioned.schema-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [versioned.schema :refer [Model]]))

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
      (is (s/validate Model valid-basic-model))
      (is (s/validate Model valid-model))
      (is (thrown-with-msg? RuntimeException #"does not match schema" (s/validate Model empty-model)))
      (is (thrown-with-msg? RuntimeException #"does not match schema"  (s/validate Model invalid-routes-model)))
    ))
