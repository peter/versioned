(ns versioned.util.core-test
  (:require [clojure.test :refer :all]
            [versioned.test-helper :as test-helper]
            [versioned.util.core :refer [deep-map-values]]))

(use-fixtures :each test-helper/fixture)

(deftest deep-map-values-test
  (testing "recurses maps and colls by default"
    (let [f #(* (:value %) 2)
          value {
            :foo 1
            :bar [2 3]
            :baz {
              :bla 4
              :blu [{:baa 5}]
            }
          }
          expected {
            :foo 2
            :bar [4 6]
            :baz {
              :bla 8
              :blu [{:baa 10}]
            }
          }]
      (is (= (deep-map-values f value) expected))))
  (testing "can recurse maps only"
    (let [f #(if (number? (:value %)) (* 2 (:value %)) nil)
          value {
            :foo 1
            :bar [2 3]
            :baz {
              :bla 4
              :blu [{:baa 5}]
            }
          }
          opts {:recurse-if? #(map? (:value %))}
          expected {
            :foo 2
            :bar nil
            :baz {
              :bla 8
              :blu nil
            }
          }]
      (is (= (deep-map-values f value opts) expected))))
  (testing "can recurse to certain depth"
    (let [f #(if (number? (:value %)) (* 2 (:value %)) nil)
          value {
            :foo 1
            :bar [2 3]
            :baz {
              :bla 4
              :blu [{:baa {:foo 5}}]
            }
          }
          opts {:recurse-if? #(< (count (:path %)) 3)}
          expected {
            :foo 2
            :bar [4 6]
            :baz {
              :bla 8
              :blu [nil]
            }
          }]
      (is (= (deep-map-values f value opts) expected)))))
