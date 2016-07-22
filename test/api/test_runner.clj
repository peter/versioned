(ns api.test-runner
  (:require [content-api :as content-api]
            [content-api.util.json :as json]
            [me.raynes.conch :as sh]
            [content-api.util.file :as file]
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
    (flatten [(:config paths) (:data-tmp paths) suites])))

(defn clear-db [context]
  context)

(defn save-db [context]
  context)

(defn restore-db [context]
  (log "restore-db")
  (-> context
      (clear-db)
      (save-db)))

(defn start-server [context]
  (log "start-server")
  (let [system (content-api/-main :models (:models context)
                                  :sites (:sites context)
                                  :locales (:locales context))]
    (assoc context :system system)))

(defn log-in-user [context]
  (log "log-in-user")
  (let [params (get-in context [:data :users :admin])
        app (get-in context [:system :app])
        response (sessions/create app {:params params})
        auth-header (get-in response [:headers "Authorization"])]
    (if auth-header
      (assoc-in context [:data :headers :admin] {"Authorization" auth-header})
      (throw (.Exception (str "Could not log in user" params "response: " response))))))

(defn write-data-tmp-file [context]
  (let [path (get-in context [:paths :data-tmp])
        data (json/generate {:data (:data context)})]
    (spit path data)
    context))

(defn run-tests [context]
  (let [test-command (flatten ["jsonapitest" (test-files context) {:out *out*}])]
    (log "run-tests" test-command)
    (apply sh/execute test-command)))

(defn -main [& args]
  (let [context (new-context)]
    (log "Starting with context" context)
    (-> context
        (restore-db)
        (start-server)
        (log-in-user)
        (write-data-tmp-file)
        (run-tests))))
