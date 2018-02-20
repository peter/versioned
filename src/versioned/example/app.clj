(ns versioned.example.app
  (:require [versioned :as versioned]
            [versioned.components.config :refer [get-env]]))

(def sites ["se" "no" "dk" "fi"])

(def default-config {
  :models {
    :sections "versioned.example.models.sections/spec"
    :pages "versioned.example.models.pages/spec"
    :widgets "versioned.example.models.widgets/spec"
    :articles "versioned.example.models.articles/spec"
  }
  :sites sites
  :locales sites
  :mongodb-url "mongodb://127.0.0.1/versioned-example"
  :search-enabled true
  :algoliasearch-index-name (str "versioned-example-" (get-env {}))
  :algoliasearch-application-id nil ; provided with env variable
  :algoliasearch-api-key nil ; provided with env variable
})

(defn -main [& {:as custom-config}]
  (let [config (merge default-config custom-config)
        args-list (apply concat (seq config))]
    (apply versioned/-main args-list)))
