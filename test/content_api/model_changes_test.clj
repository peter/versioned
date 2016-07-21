(ns content-api.model-changes-test
  (:use midje.sweet)
  (:require [content-api.model-changes :refer [changed-value? model-changes model-changed?]]))

(fact "changed-value?: returns true if from and to are equal"
  (changed-value? {:foo 1} {:foo 1}) => false)

(fact "changed-value?: returns false if from and to are not equal"
  (changed-value? {:foo 1} {:foo 2}) => true)

(fact "model-changes: returns from/to values for all changed attributes based on :existing-doc meta"
  (let [existing-doc {:changed "changed" :removed "removed"}
        new-doc {:changed "changed EDIT" :added "added"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model-spec {:schema {
          :type "object"
          :properties {
            :changed {:type "string"}
            :removed {:type "string"}
            :added {:type "string"}
          }
        }}
        expected {
          :changed {:from "changed" :to "changed EDIT"}
          :removed {:from "removed" :to nil}
          :added {:from nil :to "added"}}]
    (model-changes model-spec doc) => expected))

(fact "model-changed?: with two arguments returns true if there is any change in doc"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model-spec {:schema {
          :type "object"
          :properties {
            :title {:type "string"}
          }
        }}]
      (model-changed? model-spec doc) => truthy))

(fact "model-changed?: with two arguments returns false if there is no change in doc"
  (let [existing-doc {:title "title"}
        new-doc {:title "title"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model-spec {:schema {
          :type "object"
          :properties {
            :title {:type "string"}
          }
        }}]
      (model-changed? model-spec doc) => falsey))

(fact "model-changed?: with three arguments returns true if certain argument has changed"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model-spec {:schema {
          :type "object"
          :properties {
            :title {:type "string"}
          }
        }}]
      (model-changed? model-spec doc :title) => truthy))

(fact "model-changed?: with three arguments returns false if certain argument has not changed"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model-spec {:schema {
          :type "object"
          :properties {
            :title {:type "string"}
          }
        }}]
      (model-changed? model-spec doc :body) => falsey))

(fact "model-changed?: with five arguments returns true if certain argument has changed from one value to another"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model-spec {:schema {
          :type "object"
          :properties {
            :title {:type "string"}
          }
        }}]
      (model-changed? model-spec doc :title "title" "title changed") => truthy))

(fact "model-changed?: with five arguments returns false if certain argument has not changed from one value to another"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        model-spec {:schema {
          :type "object"
          :properties {
            :title {:type "string"}
          }
        }}]
      (model-changed? model-spec doc :title "title" "title changed different") => falsey))
