(ns http-server.http
  (:require [clojure.string :as str]))

(defn status [code]
  (let [output (case code
                 200 "200 OK\n"
                 204 "204 No Content\n"
                 206 "206 Partial Content\n"
                 302 "302 Found\n"
                 401 "401 Unauthorized\n"
                 404 "404 File not found\n"
                 405 "405 Method not allowed\n"
                 "400 Bad Request\n")]
    (str "HTTP/1.1 " output)))

(defn header [key]
  (let [type (case key
               :text "text/plain"
               :empty "empty/empty"
               "text/html")]
    (str "Server: HTTP server/0.1 Clojure/1.6.0\n"
         "Date: " (str (java.util.Date.) "\n")
         "Content-Type: "
         type ";"
         "charset=utf-8\n"
         "\n")))

(defn image-header [extension]
  (str "Server: HTTP server/0.1 Clojure/1.6.0\n"
       "Date: " (str (java.util.Date.) "\n")
       (str "Content-type: image/" extension "\n")
       "\n"))

(defn add-options-to-header [header]
  (str/replace header
               #"\n\n"
               "\nAllow: GET,HEAD,POST,OPTIONS,PUT\n\n"))

(defn add-redirect-to-header [header headers]
  (let [host (str/replace (first headers) #"Host: " "")]
    (str/replace header
                 #"\n\n"
                 (str "\nLocation: http://" host "/\n\n"))))

(defn decode-parameters [query]
  (->> (str/split query #"\&")
       (map #(java.net.URLDecoder/decode %))
       (map #(str/replace-first % "=" " = "))
       (map #(str % "\n"))
       (apply str)))

(defn remove-range [{:keys [headers] :as request}]
  (assoc-in request [:headers] (remove #(re-find #"Range:" %) headers)))
