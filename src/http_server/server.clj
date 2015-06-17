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

;; (defn server [port handler directory]
;;   (let [running (atom true)]
;;     (future 
;;       (with-open [server-socket (connect port)]
;;         (while @running
;;           (with-open [socket (.accept server-socket)]
;;             (try
;;               (send socket handler directory)
;;               (catch Throwable t (println "Error!"))
;;               ;; Better error reporting above and below
;;               (finally (try (.close socket)
;;                             (catch Throwable t (println "Error!" t)))))))))
;;     running))

;; (defn server [port directory]
;;   (with-open [server-socket (ServerSocket. port 150)]
;;     (loop [running true
;;            n 0]
;;       (let [socket (.accept server-socket)]
;;         (future
;;           (try
;;             (let [input (io/input-stream socket)
;;                   rdr (io/reader input)
;;                   [msg1 & headers] (read/read-msgs rdr [])
;;                   body (read/read-body rdr)
;;                   amsg (adaptor/adaptor msg1 headers body)
;;                   hmsg (handler/handler amsg directory)
;;                   output (io/output-stream socket)]
;;               (send hmsg output)
;;               (log/log msg1))
;;             (catch Throwable t (println "Error!")) ;; Better error reporting here and below
;;             (finally (try (.close socket)
;;                           (catch Throwable t (println "Error!" t)))))))
;;       (recur running (inc n)))))

;; WORKING WELL
;; (defn server [port handler directory]
;;   (with-open [server-socket (ServerSocket. port 150)]
;;     (while true
;;       (let [socket (.accept server-socket)]
;;         (future
;;           (try
;;             (send socket handler directory)
;;             (catch Throwable t (println "Error!")) ;; Better error reporting here and below
;;             (finally (try (.close socket)
;;                           (catch Throwable t (println "Error!" t))))))))))

;; (server/server 8888 handler/handler "/Users/robert/clojure-1.6.0/http_server/public")

;; (def a (server/server 8888 handler/handler "/Users/robert/clojure-1.6.0/http_server/public"))

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
