(defproject versioned "0.9.0"
  :description "CMS REST API based on MongoDB"
  :url "https://github.com/peter/versioned"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [com.novemberain/monger "3.0.2"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [crypto-password "0.2.0"]
                 [cheshire "5.5.0"]
                 [metosin/scjsv "0.2.0"]
                 [prismatic/schema "1.1.3"]
                 [clj-time "0.11.0"]
                ]
  :min-lein-version "2.0.0"
  :uberjar-name "versioned-standalone.jar"
  :main ^:skip-aot versioned.example.app
  :target-path "target/%s"
  :aliases {
    "test-api" ["run" "-m" "api.test-runner"]
    "test-all" ["do" "test" ["run" "-m" "api.test-runner"]]
  }
  :profiles {
    :uberjar {:aot :all}
    :dev {:dependencies [;[midje "1.9.0-alpha4"]
                         [me.raynes/conch "0.8.0"]
                        ]}
  }
)
