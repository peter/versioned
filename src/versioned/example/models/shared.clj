(ns versioned.example.models.shared
  (:require [clojure.string :as str]))

(def crud-routes [:list :get :create :update :delete])

(defn sites-schema [config] {
  :type "array"
  :items {
    :enum (:sites config)
  }
})

(defn sort-sites [config sites]
  (sort-by #(.indexOf (:sites config) %) sites))

(defn parse-sites [sites]
  (if (string? sites)
    (map str/trim (str/split sites #","))
    sites))

(defn normalize-sites [config sites]
  (->> (parse-sites sites)
       (sort-sites config)))

(defn set-sites-callback [doc options]
  (if (:sites doc)
    (assoc doc :sites (normalize-sites (get-in options [:app :config]) (:sites doc)))
    doc))
;    (assoc doc :sites (get-in options [:app :config :sites]))))

(defn translated-attribute
  ([locales custom-schema]
    ; NOTE: could use JSON schema patternProperties with (str "^(" (str/join "|" locales) ")$") here,
    ;       but patternProperties is currently not allowed by Swagger.
    (let [default-attribute-schema {:type "string"}
          attribute-schema (merge default-attribute-schema custom-schema)
          properties (reduce #(assoc %1 %2 attribute-schema) {} locales)]
      {
        :type "object"
        :meta {
          :translated true
        }
        :properties properties
        :additionalProperties false
      }))
    ([locales]
      (translated-attribute locales {})))
