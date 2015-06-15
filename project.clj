(defproject http_server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main ^:skip-aot http-server.main
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[speclj "3.2.0"]]}
             :uberjar {:aot :all}}
  :plugins [[speclj "3.2.0"]]
  :test-paths ["spec"])
