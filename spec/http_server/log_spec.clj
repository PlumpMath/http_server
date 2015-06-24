(ns http-server.log-spec
  (:require [speclj.core :refer :all]
            [http-server.log :refer :all]))

(describe "http-server.log"

  (describe "Logging"
    (let [result (spit (clojure.java.io/file "/tmp/http_server.log") "" :append false)
          result (log "GET /my-file HTTP/1.1")]
      
      (it "log go to /tmp/http_server.log"
        (should (re-find #"GET \/my-file HTTP\/1.1" (slurp (clojure.java.io/file "/tmp/http_server.log"))))))))

(run-specs)
