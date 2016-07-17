(ns content-api.crud-api
  (:require [content-api.model-api :as model-api]
            [content-api.json-api :as json-api]
            [content-api.crud-api-opts :refer [list-opts get-opts]]
            [content-api.crud-api-attributes :refer [invalid-attributes create-attributes update-attributes]]
            [content-api.crud-api-audit :refer [save-changelog]]
            [content-api.logger :as logger]
            [content-api.util.core :as u]))

(defprotocol CrudApi
  (list [api app request])
  (get [api app request])
  (create [api app request])
  (update [api app request])
  (delete [api app request]))

(defrecord BaseApi [model-spec]
  CrudApi

  (list [this app request]
    (let [opts (list-opts request)
          docs (model-api/find app model-spec {} opts)]
      (json-api/response docs)))

  (get [this app request]
    (let [doc (model-api/find-one app model-spec (json-api/id request) (get-opts request))]
      (if doc
        (json-api/response (json-api/json-doc model-spec doc))
        (json-api/missing-response))))

  (create [this app request]
    (let [invalids (invalid-attributes model-spec request)]
      (if-not invalids
        (let [attributes (->> (json-api/attributes model-spec request)
                              (create-attributes model-spec request))
              doc (model-api/create app model-spec attributes)]
          (println "pm debug create" (:type model-spec) (json-api/attributes model-spec request) (:params request))
          (logger/debug app "crud-api create" (:type model-spec) "attributes:" attributes "doc:" doc " meta:" (meta doc))
          (save-changelog (:database app) request model-spec :create doc)
          (json-api/response doc))
        (json-api/invalid-attributes-response invalids))))

  (update [this app request]
    (let [invalids (invalid-attributes model-spec request)]
      (if-not invalids
        (let [existing-doc (model-api/find-one app model-spec (json-api/id request))]
          (if existing-doc
            (let [attributes (->> (json-api/attributes model-spec request)
                                  (update-attributes model-spec request))
                  doc (model-api/update app model-spec attributes)]
              (logger/debug app "crud-api update " (:type model-spec) " doc:" doc " meta:" (meta doc))
              (save-changelog (:database app) request model-spec :update doc)
              (json-api/response doc))
            (json-api/missing-response)))
        (json-api/invalid-attributes-response invalids))))

  (delete [this app request]
    (let [existing-doc (model-api/find-one app model-spec (json-api/id request))]
      (if existing-doc
        (let [doc (model-api/delete app model-spec existing-doc)]
          (logger/debug app "crud-api delete " (:type model-spec) " doc:" doc " meta:" (meta doc))
          (save-changelog (:database app) request model-spec :delete doc)
          (json-api/response {}))
        (json-api/missing-response)))))

(defn new-api [& args]
  (map->BaseApi (apply hash-map args)))
