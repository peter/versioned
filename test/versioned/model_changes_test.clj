(ns versioned.model-changes-test
  (:require [clojure.test :refer :all]
            [versioned.model-changes :refer [changed-value? model-changes model-changed?]]))

(deftest changed-value?_returns-true-if-from-and-to-are-equal
  (is (= (changed-value? {:foo 1} {:foo 1}) false)))

(deftest changed-value?_returns-false-if-from-and-to-are-not-equal
  (is (= (changed-value? {:foo 1} {:foo 2}) true)))

(deftest model-changes_returns-from-to-values-for-all-changed-attributes-based-on-existing-doc-meta
  (let [existing-doc {:changed "changed" :removed "removed"}
        new-doc {:changed "changed EDIT" :added "added"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model {
          :type :articles
          :schema {
            :type "object"
            :properties {
              :changed {:type "string"}
              :removed {:type "string"}
              :added {:type "string"}
            }
          }
        }
        expected {
          :changed {:from "changed" :to "changed EDIT"}
          :removed {:from "removed" :to nil}
          :added {:from nil :to "added"}}]
    (is (= (model-changes model doc) expected))))

(deftest model-changed-with-two-arguments-returns-true-if-there-is-any-change-in-doc
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model {
          :type :articles
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
            }
          }
        }]
      (is (model-changed? model doc))))

(deftest model-changed-with-two-arguments-returns-false-if-there-is-no-change-in-doc
  (let [existing-doc {:title "title"}
        new-doc {:title "title"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model {
          :type :articles
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
            }
          }
        }]
      (is (not (model-changed? model doc)))))

(deftest model-changed-with-three-arguments-returns-true-if-certain-argument-has-changed
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model {
          :type :articles
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
            }
          }
        }]
      (is (model-changed? model doc :title))))

(deftest model-changed-with-three-arguments-returns-false-if-certain-argument-has-not-changed
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model {
          :type :articles
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
            }
          }
        }]
      (is (not (model-changed? model doc :body)))))

(deftest model-changed-with-five-arguments-returns-true-if-certain-argument-has-changed-from-one-value-to-another
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model {
          :type :articles
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
            }
          }
        }]
      (is (model-changed? model doc :title "title" "title changed"))))

(deftest model-changed-with-five-arguments-returns-false-if-certain-argument-has-not-changed-from-one-value-to-another
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model {
          :type :articles
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
            }
          }
        }]
      (is (not (model-changed? model doc :title "title" "title changed different")))))
