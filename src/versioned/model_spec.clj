(ns versioned.model-spec
  (:require [versioned.routes :refer [crud-actions]]
            [versioned.util.core :as u]
            [versioned.model-callbacks :refer [normalize-callbacks merge-callbacks sort-callbacks]]
            [versioned.model-relationships :refer [normalized-relationships]]
            [versioned.util.schema :refer [validate-schema]]
            [clojure.spec :as s]))

; NOTE: there is no official merge support for JSON schema AFAIK and we cannot use "allOf"
(defn merge-schemas [& schemas]
  (let [properties (apply merge (map :properties schemas))
        required (apply concat (map :required schemas))]
    (assoc (apply merge schemas)
      :properties properties
      :required required)))

(def empty-callback {:before [] :after []})

; TODO: this spec is a duplicate of the JSON schema below
(s/def ::type keyword?)
(s/def ::schema map?)
(s/def ::callbacks map?)
(s/def ::relationships map?)
(s/def ::indexes (s/coll-of map?))
; TODO: duplicate of routes/crud-actions
(s/def ::routes (s/coll-of #{:list :get :create :update :delete} :distinct true))
(s/def ::model (s/keys :req-un [::type ::schema]
                       :opt-un [::callbacks ::relationships ::indexes ::routes]))

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
  :additionalProperties false
})

(defn generate-spec [& specs]
  (let [specs (flatten specs)
        schema (apply merge-schemas (u/compact (map :schema specs)))
        callbacks (not-empty (sort-callbacks (apply merge-callbacks (map normalize-callbacks (u/compact (map :callbacks specs))))))
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
