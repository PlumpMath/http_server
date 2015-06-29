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

(defn get-range-string [headers]
  (let [range-string (filter #(re-find #"Range: bytes" %) headers)]
    (-> range-string first (str/split #"bytes=") second)))

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

(defn file-range [file headers]
  (let [file-bytes (if (string? file) file (file-to-byte-array file))
        size (count file-bytes)
        range (get-range-string headers)
        [lower upper] (get-range range size)]
    (if (string? file) (subs file lower (inc upper))
        (-> (bytes-range file-bytes lower upper)
            byte-array))))

(defn base64-to-bytes [b64]
  (try
    (into [] (b64/decode (.getBytes b64)))
    (catch Throwable t
      (errorlog/error t "Error: "))))

(defn base64-decode [s]
  (->> s base64-to-bytes (map char) (apply str)))

(defn patched-file? [uri]
  (let [TMP_DIR (System/getProperty "TMP_DIR")
        file (io/file (str TMP_DIR "/patches.edn"))
        patched-data (if (.exists file)
                       (-> file
                           slurp
                           edn/read-string))  
        name-key (keyword (subs uri 1))]
    (get-in patched-data [name-key :id])))
  
(defn add-patch [uri headers body]
  (let [TMP_DIR (System/getProperty "TMP_DIR")
        file (io/file (str TMP_DIR "/patches.edn"))
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
  (let [TMP_DIR (System/getProperty "TMP_DIR")
        file (io/file (str TMP_DIR "/patches.edn"))
        name (subs uri 1)
        patched-data (if (.exists file)
                       (-> file
                           slurp
                           edn/read-string))]
    (get-in patched-data [(keyword name) :body])))

(defn generate-form [body]
  (let [TMP_DIR (System/getProperty "TMP_DIR")
        file (io/file (str TMP_DIR "/form"))]
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
  (let [TMP_DIR (System/getProperty "TMP_DIR")]
    (io/file (str TMP_DIR "/form"))))

(defn show-logs []
  (let [TMP_DIR (System/getProperty "TMP_DIR")]
    (io/file (str TMP_DIR "/http_server.log"))))
