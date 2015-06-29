(ns http-server.log)

(defn log [s]
  (let [TMP_DIR (System/getProperty "TMP_DIR")
        file (clojure.java.io/file (str TMP_DIR "/http_server.log"))]
    (spit file (str s "\n\n") :append true)))
