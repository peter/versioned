(ns versioned.crud-api
  (:refer-clojure :exclude [list get update])
  (:require [versioned.model-api :as model-api]
            [versioned.json-api :as json-api]
            [versioned.crud-api-opts :refer [list-opts get-opts]]
            [versioned.crud-api-query :refer [list-query]]
            [versioned.crud-api-attributes :refer [read-attributes invalid-attributes create-attributes update-attributes]]
            [versioned.crud-api-audit :refer [save-changelog]]
            [versioned.logger :as logger]
            [versioned.util.core :as u]))

(defn- doc-response [model-spec doc]
  (->> (read-attributes model-spec doc)
       (json-api/doc-response model-spec)))

(defprotocol CrudApi
  (list [api app request])
  (get [api app request])
  (create [api app request])
  (update [api app request])
  (delete [api app request]))

(defrecord BaseApi [model-spec]
  CrudApi

  (list [this app request]
    (let [query (list-query model-spec request)
          opts (list-opts model-spec request)
          docs (model-api/find app model-spec query opts)
          read-docs (map (partial read-attributes model-spec) docs)]
      (json-api/docs-response model-spec read-docs)))

  (get [this app request]
    (let [doc (model-api/find-one app model-spec (json-api/id request) (get-opts request))]
      (if doc
        (doc-response model-spec doc)
        (json-api/missing-response))))

  (create [this app request]
    (let [invalids (invalid-attributes model-spec request)]
      (if-not invalids
        (if-let [attributes (some->> (json-api/attributes request)
                                     (create-attributes model-spec request))]
          (let [doc (model-api/create app model-spec attributes)]
            (logger/debug app "crud-api create" (:type model-spec) "attributes:" attributes "doc:" doc "meta:" (meta doc))
            (save-changelog app request model-spec :create doc)
            (doc-response model-spec doc))
          (json-api/missing-attributes-response))
        (json-api/invalid-attributes-response invalids))))

  (update [this app request]
    (let [invalids (invalid-attributes model-spec request)]
      (if-not invalids
        (let [existing-doc (model-api/find-one app model-spec (json-api/id request))]
          (if existing-doc
            (if-let [attributes (some->> (json-api/attributes request)
                                         (update-attributes model-spec request))]
              (let [doc (model-api/update app model-spec attributes)]
                (logger/debug app "crud-api update" (:type model-spec) "doc:" doc "meta:" (meta doc))
                (save-changelog app request model-spec :update doc)
                (doc-response model-spec doc))
              (json-api/missing-attributes-response))
            (json-api/missing-response)))
        (json-api/invalid-attributes-response invalids))))

  (delete [this app request]
    (let [existing-doc (model-api/find-one app model-spec (json-api/id request))]
      (if existing-doc
        (let [doc (model-api/delete app model-spec existing-doc)]
          (logger/debug app "crud-api delete" (:type model-spec) "doc:" doc "meta:" (meta doc))
          (save-changelog app request model-spec :delete doc)
          (doc-response model-spec doc))
        (json-api/missing-response)))))

(defn new-api [& args]
  (map->BaseApi (apply hash-map args)))
