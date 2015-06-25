(ns http-server.log-spec
  (:require [speclj.core :refer :all]
            [http-server.log :refer :all]))

(defn get-file-from-tmp [file]
  (str (System/getProperty "TMP_DIR")
       "/" file))

(describe "http-server.log"
  (around [it]
          (System/setProperty "TMP_DIR" "tmp")
          (it))

  (describe "Logging"
    (let [result (spit (clojure.java.io/file
                        (get-file-from-tmp "http_server.log")) "" :append false)
          result (log "GET /my-file HTTP/1.1")]
      
      (it "log go to TMP_DIR/http_server.log"
        (should (re-find #"GET \/my-file HTTP\/1.1"
                         (slurp (clojure.java.io/file
                                 (get-file-from-tmp "http_server.log")))))))))

(run-specs)
