(ns versioned.crud-api-types-test
  (:require [clojure.test :refer :all]
            [versioned.crud-api-types :refer [coerce-attribute-types]]
            [versioned.util.date :as d]))

(deftest coerce-attribute-types_nullifies-blank-values-in-nested-attributes
  (let [attributes {
          :title "  "
          :item {
            :values ["foo" "" "bar"]
          }
        }
        schema {
          :type "object"
          :properties {
            :title {:type "string"}
            :item {
              :type "object"
              :properties {
                :values {:type "array"}
              }
            }
          }
        }
        expected {
          :title nil
          :item {
            :values ["foo" "bar"]
          }
        }]
    (is (= (coerce-attribute-types schema attributes) expected))))

(deftest coerce-attribute-leaves_unparsable_values_untouched
  (let [attributes {
          :foo "this-is-not-a-valid-number"
          :bar "123"
        }
        schema {
          :type "object"
          :properties {
            :foo {:type "integer"}
            :bar {:type "integer"}
          }
        }
        expected {
          :foo "this-is-not-a-valid-number"
          :bar 123
        }]
    (is (= (coerce-attribute-types schema attributes) expected))))

(deftest coerce-attribute-types_parses-dates-in-nested-attributes
  (let [attributes {
          :title "foobar"
          :created_at "2016-03-24T08:39:42.432+02:00"
          :item {
            :dates ["2016-04-24T08:39:42.432+02:00" "" "2016-05-24T08:39:42.432+02:00"]
          }
        }
        schema {
          :type "object"
          :properties {
            :title {:type "string"}
            :created_at {:type "string" :format "date-time"}
            :item {
              :type "object"
              :properties {
                :dates {
                  :type "array"
                  :items {:type "string" :format "date-time"}
                }
              }
            }
          }
        }
        expected {
          :title "foobar"
          :created_at (d/parse-datetime "2016-03-24T08:39:42.432+02:00")
          :item {
            :dates [(d/parse-datetime "2016-04-24T08:39:42.432+02:00") (d/parse-datetime "2016-05-24T08:39:42.432+02:00")]
          }
        }]
    (is (= (coerce-attribute-types schema attributes) expected))))
