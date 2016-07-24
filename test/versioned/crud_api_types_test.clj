(ns versioned.crud-api-types-test
  (:use midje.sweet)
  (:require [versioned.crud-api-types :refer [coerce-attribute-types]]
            [versioned.util.date :as d]))

(fact "coerce-attribute-types: nullifies blank values in nested attributes"
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
    (coerce-attribute-types schema attributes) => expected))

(fact "coerce-attribute-types parses dates in nested attributes"
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
    (coerce-attribute-types schema attributes) => expected))
