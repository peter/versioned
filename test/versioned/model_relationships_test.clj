(ns versioned.model-relationships-test
  (:use midje.sweet)
  (:require [versioned.model-relationships :refer [relationship-spec]]))

(fact "relationship-spec: can default from_coll/from_field/to_coll/to_field for has-many"
  (let [relationship :widgets
        model-spec {
          :type :pages
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
              :widgets_ids {:type "array"}
            }
          }
          :relationships {
            :widgets {}
          }
        }
        expected {
          :from_coll :pages
          :from_model :pages
          :from_field :widgets_ids
          :to_coll :widgets
          :to_model :widgets
          :to_field :id
        }]
    (relationship-spec relationship model-spec) => expected))

(fact "relationship-spec: can default from_coll/from_field/to_coll/to_field for has-one"
  (let [relationship :widgets
        model-spec {
          :type :pages
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
              :widgets_id {:type "integer"}
            }
          }
          :relationships {
            :widgets {}
          }
        }
        expected {
          :from_coll :pages
          :from_model :pages
          :from_field :widgets_id
          :to_coll :widgets
          :to_model :widgets
          :to_field :id
        }]
    (relationship-spec relationship model-spec) => expected))

(fact "relationship-spec: can specify from_coll/from_field/to_coll/to_field"
  (let [relationship :widgets
        options {
          :from_coll :foobar
          :from_model nil
          :from_field :foobar_ids
          :to_coll :foo
          :to_model nil
          :to_field :bar
        }
        model-spec {
          :type :pages
          :schema {
            :type "object"
            :properties {
              :title {:type "string"}
              :widgets_id {:type "integer"}
            }
          }
          :relationships {
            :widgets options
          }
        }
        expected options]
    (relationship-spec relationship model-spec) => expected))
