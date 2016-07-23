(ns api.test-runner
  (:require [content-api :as content-api]
            [content-api.util.json :as json]
            [content-api.crud-api-attributes :refer [create-attributes read-attributes]]
            [content-api.model-api :as model-api]
            [content-api.model-validations :refer [model-errors]]
            [me.raynes.conch :as sh]
            [monger.db :refer [drop-db]]
            [content-api.util.file :as file]
            [content-api.util.core :as u]
            [content-api.controllers.sessions :as sessions]
            [clojure.string :as str]))

(defn log [& args]
  (apply println "[api.test-runner]" args))

(defn read-data [path]
  (:data (json/parse (slurp path))))

(defn new-context []
  (let [models {:sections "content-api.example-models.sections/spec"
                :pages "content-api.example-models.pages/spec"
                :widgets "content-api.example-models.widgets/spec"}
        sites ["se" "no" "dk" "fi"]
        locales sites
        paths {
          :jsonapitest (or (System/getenv "JSONAPITEST_PATH") "jsonapitest")
          :config "test/api/config.js"
          :data "test/api/data.json"
          :data-tmp "test/api/data.tmp.json"
          :test-suites "test/api/suites"
        }]
  {
    :paths paths
    :data (read-data (:data paths))
    :models models
    :sites sites
    :locales locales
  }))

(defn test-files [context]
  (let [paths (:paths context)
        suites (file/ls (:test-suites paths) :ext ".js")]
    (flatten [(:data-tmp paths) (:config paths) suites])))

(defn clear-db [context]
  (let [db (get-in context [:system :database :db])]
    (drop-db db)
    context))

(defn save-doc [app model-spec request doc]
  (let [attributes (merge (create-attributes model-spec request doc)
                          (select-keys doc [:id]))
        saved-doc (model-api/create app model-spec attributes)
        errors (model-errors saved-doc)
        result (merge doc (read-attributes model-spec saved-doc))]
    (if errors
        (throw (Exception. (str "Could not save doc to db doc: " doc " errors: " errors)))
        result)))

(defn save-model [context model data]
  (let [app (get-in context [:system :app])
        model-spec (get-in app [:models model])
        admin-user (get-in context [:data :users :admin])
        request {:user admin-user}]
    (u/map-values (partial save-doc app model-spec request) data)))

(defn save-db [context]
  (let [model? (set (keys (get-in context [:system :app :models])))
        data (reduce
                (fn [result [k v]]
                  (assoc result k (if (model? k)
                                      (save-model context k v)
                                      v)))
                {}
                (:data context))]
    (assoc context :data data)))

(defn restore-db [context]
  (log "restore-db")
  (-> context
      (clear-db)
      (save-db)))

(defn start-server [context]
  (log "start-server")
  (let [system (content-api/-main :env "test"
                                  :models (:models context)
                                  :sites (:sites context)
                                  :locales (:locales context))]
    (assoc context :system system)))

(defn log-in-user [context]
  (log "log-in-user")
  (let [params (select-keys (get-in context [:data :users :admin]) [:email :password])
        app (get-in context [:system :app])
        response (sessions/create app {:params params})
        auth-header (get-in response [:headers "Authorization"])]
    (if auth-header
      (assoc-in context [:data :headers :admin] {"Authorization" auth-header})
      (throw (Exception. (str "Could not log in user " params " response: " response))))))

(defn add-schemas [context]
  (let [models (get-in context [:system :app :models])
        schemas (u/map-values :schema models)]
    (assoc-in context [:data :schema] schemas)))

(defn write-data-tmp-file [context]
  (let [path (get-in context [:paths :data-tmp])
        data (json/generate {:data (:data context)})]
    (spit path data)
    context))

(defn run-tests [context]
  (let [test-command (flatten [(get-in context [:paths :jsonapitest]) (test-files context) {:out *out*}])]
    (log "run-tests" test-command)
    (apply sh/execute test-command)))

(defn -main [& args]
  (let [context (new-context)]
    (log "Starting with context" context)
    (-> context
        (start-server)
        (restore-db)
        (log-in-user)
        (add-schemas)
        (write-data-tmp-file)
        (run-tests))))
