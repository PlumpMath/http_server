(ns http-server.files
  (:require [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64]
            [clojure.tools.logging :as errorlog]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn file-to-byte-array [file]
  (let [result (byte-array (.length file))]
    (with-open [in (java.io.DataInputStream. (io/input-stream file))]
      (.readFully in result))
    result))

(defn get-range [range size]
  (let [[left right] (str/split range #"-")]
    (cond (and (seq left) (seq right)) ;; e.g. "0-4"
          [(edn/read-string left) (edn/read-string right)]
          (and (seq left) (not (seq right))) ;; e.g. "4-"
          [(edn/read-string left) (dec size)]
          :else
          [(- size (edn/read-string right)) (dec size)])))

(defn bytes-range [file-bytes lower upper]
  (let [size (inc (- upper lower))]
    (take size (drop lower file-bytes))))

(defn file-range [uri headers]
  (let [PUB_DIR (System/getProperty "PUB_DIR")
        file (io/file (str PUB_DIR uri))
        file-bytes (file-to-byte-array file)
        size (count file-bytes)
        range (-> headers first (str/split #"bytes=") second)
        [lower upper] (get-range range size)
        bytesrange (bytes-range file-bytes lower upper)]
    (byte-array bytesrange)))

(defn base64-to-bytes [b64]
  (try
    (into [] (b64/decode (.getBytes b64)))
    (catch Throwable t
      (errorlog/error t "Error: "))))

(defn base64-decode [s]
  (->> s base64-to-bytes (map char) (apply str)))

(defn patched-file? [uri]
  (let [file (io/file "tmp/patches.edn")
        patched-data (if (.exists file)
                       (-> file
                           slurp
                           edn/read-string))  
        name-key (keyword (subs uri 1))]
    (get-in patched-data [name-key :id])))
  
(defn add-patch [uri headers body]
  (let [file (io/file "tmp/patches.edn")
        patched-data (if (.exists file)
                       (-> file
                           slurp
                           edn/read-string))
        name-key (keyword (subs uri 1))
        patch-id (-> headers first (str/split #"If-Match: ") second)
        patched-data (-> patched-data
                         (assoc-in [name-key :id] patch-id)
                         (assoc-in [name-key :body] body))]
    (spit file (.toString patched-data))))

(defn show-patched-file [uri]
  (let [file (io/file "tmp/patches.edn")
        name (subs uri 1)
        patched-data (if (.exists file)
                       (-> file
                           slurp
                           edn/read-string))]
    (get-in patched-data [(keyword name) :body])))

(defn generate-form [body]
  (let [file (io/file "tmp/form")]
    (spit file (str body "\n") :append true)))

(defn generate-empty-form []
  (let [file (io/file "tmp/form")]
    (spit file (str "<HTML>\n"
                    "<head></head>\n"
                    "<body>\n"
                    "<form action=\"/form\" method=\"post\">\n"
                    "<input type=\"text\" name=\"stuff\" />\n"
                    "<input type=\"submit\" value=\"Submit\" />\n"
                    "</body>\n"
                    "</HTML>\n\n"))))

(defn show-file [uri]
  (let [PUB_DIR (System/getProperty "PUB_DIR")]
    (io/file (str PUB_DIR uri))))

(defn show-form []
  (io/file "tmp/form"))

(defn show-logs []
  (io/file "tmp/http_server.log"))

