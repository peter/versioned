(defproject versioned "0.2.0"
  :description "CMS REST API based on MongoDB"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [com.novemberain/monger "3.0.2"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [crypto-password "0.2.0"]
                 [cheshire "5.5.0"]
                 [metosin/scjsv "0.2.0"]
                 [clj-time "0.11.0"]
                ]
  :min-lein-version "2.0.0"
  :uberjar-name "versioned-standalone.jar"
  :main ^:skip-aot versioned.example.app
  :target-path "target/%s"
  :aliases {
    "unit-test" ["midje"]
    "api-test" ["run" "-m" "api.test-runner"]
    "test" ["do" "midje" ["run" "-m" "api.test-runner"]]
  }
  :profiles {
    :uberjar {:aot :all}
    :dev {:dependencies [[midje "1.6.3"]
                         [me.raynes/conch "0.8.0"]]}})
