(ns versioned.crud-api-query-test
  (:require [clojure.test :refer :all]
            [versioned.crud-api-query :refer [parse-query list-query]]))

(deftest parse-query-test
  (testing "single value with default sep"
    (is (= (parse-query "id:5") {:sep "," :field :id :values ["5"]})))
  (testing "single value with nested key and default sep"
    (is (= (parse-query "slug.se:kampanj") {:sep "," :field :slug.se :values ["kampanj"]})))
  (testing "multiple values with default sep"
    (is (= (parse-query "id:3,6,9") {:sep "," :field :id :values ["3" "6" "9"]})))
  (testing "single value with custom sep"
    (is (= (parse-query "|:title:hello") {:sep "|" :field :title :values ["hello"]})))
  (testing "single value with nested key and custom sep"
    (is (= (parse-query "|:title.se:hello") {:sep "|" :field :title.se :values ["hello"]})))
  (testing "multiple values with custom sep"
    (is (= (parse-query "|:title.se:hello, |there") {:sep "|" :field :title.se :values ["hello, " "there"]})))
  (testing "multiple values with custom sep and key with non a-z chars"
    (is (= (parse-query "|:My-weird_key.1:hello, |there") {:sep "|" :field :My-weird_key.1 :values ["hello, " "there"]}))))

(deftest list-query-test
  (testing "query with multiple values and integer type"
    (let [schema {
            :type "object"
            :properties {
              :id {:type "integer"}
              :slug {:type "string"}
              :title {:type "object"}
            }
          }
          model {:type :articles :schema schema}
          request {:query-params {:q '("id:3"
                                       "id:5"
                                       "slug:the-slug"
                                       "|:title.se:hello, |there"
                                       "not.valid.path:foo")}}
          expected {:id {:$in [3 5]}
                    :slug "the-slug"
                    :title.se {:$in ["hello, " "there"]}
                    :not.valid.path "foo"}]
      (is (= (list-query model request) expected)))))
