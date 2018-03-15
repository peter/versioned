(ns versioned.example.search.algolia
  (:require [clj-http.client :as client]
            [versioned.model-support :refer [coll]]
            [versioned.model-init :refer [get-models get-in-model]]
            [versioned.db-api :as db]))

(defn base-url [application-id]
  (str "https://" application-id ".algolia.net"))

(defn index-url [application-id index-name operation]
  (str (base-url application-id) "/1/indexes/" index-name "/" (name operation)))

(defn object-url [application-id index-name object-id]
  (str (base-url application-id) "/1/indexes/" index-name "/" object-id))

(defn application-id [app]
  (get-in app [:config :algoliasearch-application-id]))

(defn api-key [app]
  (get-in app [:config :algoliasearch-api-key]))

(defn headers [app]
  {"X-Algolia-Application-Id" (application-id app) "X-Algolia-API-Key" (api-key app)})

(defn index-name [app]
  (get-in app [:config :algoliasearch-index-name]))

(defn searchable-model? [app model-spec]
  (get-in-model app [(:type model-spec) :schema :x-meta :searchable]))

(defn colls [app]
  (let [searchable-models (filter (partial searchable-model? app) (vals (get-models app)))]
    (map coll searchable-models)))

(defn search-doc [app doc]
  (let [coll (keyword (:type doc))
        id (or (:id doc) (:_id doc))
        object-id (str (name coll) "-" id)
        type-id (.indexOf (colls app) coll)]
    (merge {:name (:title doc)} doc {
      :objectID object-id
      :type_id type-id
    })))

(defn index-request [app doc]
  {
    :action "updateObject"
    :body (search-doc app doc)
  })

(defn enabled? [app]
  (and (get-in app [:config :search-enabled])
       (application-id app)
       (api-key app)
       (index-name app)))

(defn save [app doc]
  (when (enabled? app)
    (let [sdoc (search-doc app doc)
          url (object-url (application-id app) (index-name app) (:objectID sdoc))]
      (println "search/upsert" sdoc)
      (client/put url {:headers (headers app) :form-params sdoc :content-type :json :as :json}))))

(defn delete [app doc]
  (when (enabled? app)
    (let [sdoc (search-doc app doc)
          url (object-url (application-id app) (index-name app) (:objectID sdoc))]
      (println "search/delete" sdoc)
      (client/delete url {:headers (headers app) :form-params sdoc :content-type :json :as :json}))))

(defn index-add-coll [app coll]
  (let [docs (db/find (:database app) coll {} {:per-page 50000})
        requests (map (partial index-request app) docs)
        body {:requests requests}
        url (index-url (application-id app) (index-name app) :batch)]
    (println "search/index-add-coll" coll (count requests) url)
    (client/post url {:headers (headers app) :form-params body :content-type :json :as :json})))

(defn index-clear [app]
  (let [url (index-url (application-id app) (index-name app) :clear)]
    (println "search/index-clear" url)
    (client/post url {:headers (headers app)})))

(defn index-rebuild [app]
  (index-clear app)
  (doseq [coll (colls app)]
    (index-add-coll app coll)))
