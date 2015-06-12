(ns http-server.files
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn file-to-byte-array [file]
  (let [result (byte-array (.length file))]
    (with-open [in (java.io.DataInputStream. (io/input-stream file))]
      (.readFully in result))
    result))

(defn get-upper [range size]
  (let [[left right] (str/split range #"-")]
    (if (and (seq left) (seq right)) ; e.g. "0-4"
      (inc (read-string right)) ; e.g. return 4+1
      (inc size)))) ; otherwise upper is size of file+1, e.g. "-6" or "4-"

(defn get-lower [range size]
  (let [[left right] (str/split range #"-")]
    (if (and (not (seq left)) (seq right)) ; e.g. "-6"
      (- size (read-string right)) ; e.g. return 8 if file size is 14
      (read-string left)))) ; e.g. return 2 if range is "2-4" or "2-"

(defn bytes-range [file-bytes lower upper]
  (let [size (- upper lower)]
    (take size (drop lower file-bytes))))

(defn file-range [uri headers]
  (let [file (io/file (str "public" uri))
        file-bytes (file-to-byte-array file)
        size (count file-bytes)
        range (-> headers first (str/split #"bytes=") second)
        upper (get-upper range size)
        lower (get-lower range size)
        bytesrange (bytes-range file-bytes lower upper)]
    (->> bytesrange (map char) (apply str))))

(defn base64-to-bytes [b64]
  (into [] (.decode (java.util.Base64/getDecoder) b64)))

(defn base64-decode [s]
  (->> s base64-to-bytes (map char) (apply str)))

(defn generate-file [uri body]
  (let [file (io/file (str "public" uri))]
    (spit file body)))
