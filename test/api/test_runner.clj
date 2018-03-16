(ns api.test-runner
  (:require [versioned :as versioned]
            [versioned.components.config :refer [mongodb-url]]
            [versioned.util.json :as json]
            [versioned.crud-api-attributes :refer [create-attributes read-attributes]]
            [versioned.model-api :as model-api]
            [versioned.model-validations :refer [model-errors]]
            [versioned.util.model :refer [get-models get-model]]
            [me.raynes.conch :as sh]
            [versioned.util.file :as file]
            [versioned.util.core :as u]
            [versioned.controllers.sessions :as sessions]
            [clojure.string :as str]))

(defn log [& args]
  (apply println "[api.test-runner]" args))

(defn read-data [path]
  (:data (json/parse (slurp path))))

(defn new-context []
  (let [env "test"
        models {:sections "versioned.example.models.sections/spec"
                :pages "versioned.example.models.pages/spec"
                :widgets "versioned.example.models.widgets/spec"}
        sites ["se" "no" "dk" "fi"]
        locales sites
        paths {
               :jsonapitest (or (System/getenv "JSONAPITEST_PATH") "jsonapitest")
               :config "test/api/config.js"
               :data "test/api/data.json"
               :data-tmp "test/api/data.tmp.json"
               :test-suites "test/api/suites"}]

   {
     :mongodb-url (mongodb-url env)
     :paths paths
     :data (read-data (:data paths))
     :models models
     :sites sites
     :locales locales
     :port 5001}))


(defn test-files [context]
  (let [paths (:paths context)
        suites (file/ls (:test-suites paths) :ext ".js")]
    (flatten [(:data-tmp paths) (:config paths) suites])))

(defn drop-db [context]
  (log "drop-db")
  (let [db-url (get-in context [:mongodb-url])
        drop-file "test/api/drop_db.js"]
    (sh/execute "mongo" db-url drop-file)
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
        model-spec (get-model app model)
        admin-user (get-in context [:data :users :admin])
        request {:user admin-user}]
    (u/map-values (partial save-doc app model-spec request) data)))

(defn save-db [context]
  (log "save-db")
  (let [app (get-in context [:system :app])
        model? (set (keys (get-models app)))
        data (reduce
                (fn [result [k v]]
                  (assoc result k (if (model? k)
                                      (save-model context k v)
                                      v)))
                {}
                (:data context))]
    (assoc context :data data)))

(defn start-server [context]
  (log "start-server")
  (let [system (versioned/-main :env (:env context)
                                :mongodb-url (:mongodb-url context)
                                :port (:port context)
                                :models (:models context)
                                :sites (:sites context)
                                :locales (:locales context))]
    (assoc context :system system)))

(defn log-in-user [context user]
  (log "log-in-user" user)
  (let [params (select-keys (get-in context [:data :users user]) [:email :password])
        app (get-in context [:system :app])
        response (sessions/create app {:params params})
        auth-header (get-in response [:headers "Authorization"])]
    (if auth-header
      (assoc-in context [:data :headers user] {"Authorization" auth-header})
      (throw (Exception. (str "Could not log in user " user params " response: " response))))))

(defn log-in-users [context]
  (let [users (keys (get-in context [:data :users]))]
    (reduce log-in-user context users)))

(defn add-schemas [context]
  (let [app (get-in context [:system :app])
        models (get-models app)
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
        (drop-db)
        (start-server)
        (save-db)
        (log-in-users)
        (add-schemas)
        (write-data-tmp-file)
        (run-tests))))
