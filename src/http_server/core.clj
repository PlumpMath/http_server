(ns http-server.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [http-server.handler :as handler])
  (:refer-clojure :exclude [send])
  (:import [java.net ServerSocket]))

(def ^:dynamic *cr*  0x0d)
(def ^:dynamic *lf*  0x0a)
(defn- cr? [c] (= c *cr*))
(defn- lf? [c] (= c *lf*))

(defn get-filetype [uri]
  (when (re-find #"\." uri)
    (last (str/split uri #"\."))))

(defn adaptor [msg headers body]
  (let [[method uri version] (str/split msg #" ")
        [uri query] (if uri (-> uri (str/split #"\?")) [nil nil])
        extension (get-filetype uri)]
    {:method (condp = method
               "GET" :get
               "HEAD" :head
               "POST" :post
               "PUT" :put
               "DELETE" :delete
               "OPTIONS" :options
               "TRACE" :trace
               "CONNECT" :connect
               "PATCH" :patch
               :unknown),
     :uri uri,
     :extension extension,
     :query query,
     :version version,
     :headers headers,
     :body body}))

(defn log [request]
  (spit "http_server.log" (str request) :append true))

(defn read-line-crlf [rdr]
  (loop [line []
         c (.read rdr)]
    (when (< c 0)
      (throw (Exception. (str "Error reading line: "
                              "EOF reached before CR/LF sequence"))))
    (if (cr? c)
      (let [next (.read rdr)]
        (if (lf? next)
          (apply str line)
          (throw (Exception. "Error reading line: Missing LF"))))
      (recur (conj line (char c))
             (.read rdr)))))

(defn read-msgs [rdr res]
  (let [msg (read-line-crlf rdr)]
    (if (empty? msg) res
        (read-msgs rdr (conj res msg)))))

(defn read-while-ready [rdr res]
  (if-not (.ready rdr)
    res
    (read-while-ready rdr (conj res (.read rdr)))))

(defn read-body [rdr]
  (->> (read-while-ready rdr [])
       (map char)
       (apply str)))

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
                  [msg1 & headers] (read-msgs rdr [])
                  body (read-body rdr)
                  amsg (adaptor msg1 headers body)
                  hmsg (handler/handler amsg)
                  output (io/output-stream socket)]
              (send hmsg output)
              (log msg1))
            (catch Throwable t (println "Error!"))
            (finally (try (.close socket)
                          (catch Throwable t (println "Error!" t)))))))
      (recur running (inc n)))))

(defn -main [& args]
  (let [[options args banner]
        (cli/cli args
                 ["-p" "--port" "Listen on this port" :default "5000"]
                 ["-d" "--directory" :default "public/"])
        port (Integer. (get options :port))]
    (server port)))
