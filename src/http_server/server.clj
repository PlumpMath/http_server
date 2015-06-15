(ns http-server.server
  (:require [clojure.java.io :as io]
            [http-server.adaptor :as adaptor]
            [http-server.handler :as handler]
            [http-server.read :as read]
            [http-server.log :as log])
  (:refer-clojure :exclude [send])
  (:import [java.net ServerSocket]))

(defn send [hmsg output]
  (io/copy (hmsg :status) output)
  (io/copy (hmsg :header) output)
  (io/copy (hmsg :body) output)
  (.flush output))

(defn server [port]
  (with-open [server-socket (ServerSocket. port 150)]
    (loop [running true
           n 0]
      (let [socket (.accept server-socket)]
        (future
          (try
            (let [input (io/input-stream socket)
                  rdr (io/reader input)
                  [msg1 & headers] (read/read-msgs rdr [])
                  body (read/read-body rdr)
                  amsg (adaptor/adaptor msg1 headers body)
                  hmsg (handler/handler amsg)
                  output (io/output-stream socket)]
              (send hmsg output)
              (log/log msg1))
            (catch Throwable t (println "Error!")) ;; Better error reporting here and below
            (finally (try (.close socket)
                          (catch Throwable t (println "Error!" t)))))))
      (recur running (inc n)))))
