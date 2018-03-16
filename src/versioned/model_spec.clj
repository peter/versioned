(ns versioned.model-spec
  (:require [versioned.util.core :as u]
            [versioned.util.schema :refer [merge-schemas]]
            [versioned.model-callbacks :refer [normalize-callbacks merge-callbacks sort-callbacks]]
            [versioned.model-relationships :refer [normalized-relationships]]
            [versioned.util.schema :refer [validate-schema]]
            [schema.core :as s]
            [versioned.types :refer [crud-actions
                                     Coll
                                     Schema
                                     Model]]))

(def empty-callback {:before [] :after []})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; JSON schema definition for model
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def spec-schema {
  :type "object"
  :definitions {
    :callback {
      :type "object"
      :properties {
        :before {:type "array"}
        :after {:type "array"}
      }
      :additionalProperties false
    }
  }
  :properties {
    :type {
      :type "string"
    }
    :schema {
      :type "object"
    }
    :callbacks {
      :type "object"
      :properties {
        :create {"$ref" "#/definitions/callback"}
        :update {"$ref" "#/definitions/callback"}
        :delete {"$ref" "#/definitions/callback"}
      }
      :additionalProperties false
    }
    :relationships {
      :type "object"
      :patternProperties {
        "^[a-z_]+$" {
          :type "object"
          :properties {
            :from_coll {:type "string"}
            :from_model {:type ["null", "string"]}
            :from_field {:type "string"}
            :to_field {:type "string"}
            :to_coll {:type "string"}
            :to_model {:type ["null", "string"]}
            :find_opts {
              :type "object"
              :properties {
                :sort {:type "object"}
                :per-page {:type "integer"}
                :fields {:type "array"}
              }
              :additionalProperties false
            }
          }
          :required [:from_coll :from_field :to_field :to_coll]
          :additionalProperties false
        }
      }
      :additionalProperties false
    }
    :indexes {
      :type "array"
      :items {
        :type "object"
        :properties {
          :fields {:type "array"}
          :unique {:type "boolean"}
          :coll {:type "string"}
        }
        :required [:fields]
      }
    }
    :routes {
      :type "array"
      :items {
        :enum crud-actions
      }
    }
  }
  :required [:type :schema]
})

(s/defn generate-spec :- Model
  [& specs :- [Coll]]
  (let [specs (flatten specs)
        schema (apply merge-schemas (u/compact (map :schema specs)))
        callbacks (some->> (map :callbacks specs)
                           (u/compact)
                           (not-empty)
                           (map normalize-callbacks)
                           (apply merge-callbacks)
                           (sort-callbacks))
        relationships (apply u/deep-merge (u/compact (map normalized-relationships specs)))
        indexes (flatten (u/compact (map :indexes specs)))
        merged-spec (apply merge specs)
        result (u/compact (assoc merged-spec
                            :schema schema
                            :callbacks callbacks
                            :relationships relationships
                            :indexes indexes))
        errors (validate-schema spec-schema result)]
    (if errors
      (throw (Exception. (str "Model spec " (:type result) " has an invalid structure: " (pr-str errors) " spec: " (pr-str result))))
      result)))
