(ns http-server.files
  (:require [clojure.java.io :as io]
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
          [(edn/read-string left) (inc (edn/read-string right))]
          (and (seq left) (not (seq right))) ;; e.g. "4-"
          [(edn/read-string left) (inc size)]
          :else
          [(- size (edn/read-string right)) (inc size)])))

(defn bytes-range [file-bytes lower upper]
  (let [size (- upper lower)]
    (take size (drop lower file-bytes))))

(defn file-range [uri headers directory]
  (let [PUB_DIR directory
        file (io/file (str PUB_DIR uri))
        file-bytes (file-to-byte-array file)
        size (count file-bytes)
        range (-> headers first (str/split #"bytes=") second)
        [lower upper] (get-range range size)
        bytesrange (bytes-range file-bytes lower upper)]
    (->> bytesrange (map char) (apply str))))

(defn base64-to-bytes [b64]
  (into [] (.decode (java.util.Base64/getDecoder) b64)))

(defn base64-decode [s]
  (->> s base64-to-bytes (map char) (apply str)))

(defn generate-file [uri body directory]
  (let [PUB_DIR directory
        file (io/file (str PUB_DIR uri))]
    (spit file body)))

(defn generate-form [body directory]
  (let [PUB_DIR directory
        file (io/file (str PUB_DIR "/form"))]
    (spit file (str body "\n") :append true)))

(defn empty-form [directory]
  (let [PUB_DIR directory
        file (io/file (str PUB_DIR "/form"))]
    (spit file (str "<HTML>\n"
                    "<head></head>\n"
                    "<body>\n"
                    "<form action=\"/form\" method=\"post\">\n"
                    "<input type=\"text\" name=\"stuff\" />\n"
                    "<input type=\"submit\" value=\"Submit\" />\n"
                    "</body>\n"
                    "</HTML>\n"))))

(defn show-file [uri directory]
  (let [PUB_DIR directory]
    (io/file (str PUB_DIR uri))))

(defn show-logs []
  (io/file "http_server.log"))

