(ns http-server.server
  (:require [clojure.java.io :as io]
            [http-server.adaptor :as adaptor]
            [http-server.read :as read]
            [http-server.log :as log])
  (:refer-clojure :exclude [send])
  (:import [java.net ServerSocket]))

(defn connect [port] (new ServerSocket port 150))

(defn send [socket handler directory]
  (let [input (io/input-stream socket)
        rdr (io/reader input)
        [msg1 & headers] (read/read-msgs rdr [])
        body (read/read-body rdr)
        amsg (adaptor/adaptor msg1 headers body)
        hmsg (handler amsg directory)
        output (io/output-stream socket)]
    (io/copy (hmsg :status) output)
    (io/copy (hmsg :header) output)
    (io/copy (hmsg :body) output)
    (.flush output)
    (log/log msg1)))

(defn server [port handler directory]
  (let [running (atom true)]
    (future
      (with-open [server-socket (ServerSocket. port 150)]
        (while @running
          (let [socket (.accept server-socket)]
            (future
              (try
                (send socket handler directory)
                (catch Throwable t (println "Error!"))
                ;; Better error reporting here and below
                (finally (try (.close socket)
                              (catch Throwable t (println "Error!" t))))))))))
    running))
