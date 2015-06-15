(ns http-server.operations
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

;; What do you do?
;; Building a response
;; What would be a potential use case for making a change to this file?
;; Who would request such a change?
;; Support more file types?
;; Support a new specification of HTTP?
;; Change the representation of the form (file vs atom vs something else?)

;; WHO?
;; The communicator wants to have a certain layout to the output?
;; That is, wants to change the FORMAT of the output
;; Could also be about changing CONTENT of the output?
;;


(defn status [code]
  (let [output (case code
                 200 " OK\n"
                 204 " No Content\n"
                 206 " Partial Content\n"
                 302 " Found\n"
                 401 " Unauthorized\n"
                 404 " File not found\n"
                 405 " Method not allowed\n")]
    (str "HTTP/1.1 " code output)))

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

(defn empty-body []
  (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n"
       "<html>\n"
       "<body>\n"
       "EMPTY"
       "</body>\n"
       "</html>\n"))

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

(defn show-index [directory]
  (let [PUB_DIR directory
        files (rest (file-seq (io/file PUB_DIR)))
        links (map #(str "<li><a href=\"" (str "/" (.getName %))
                         "\">" (.getName %) "</a>\n") files)]
    (-> (empty-body)
        (str/replace #"EMPTY"
                     (str "<h2>Directory listing for /</h2>"
                          "<hr>\n"
                          "<ul>\n"
                          (apply str links)
                          "</ul>\n"
                          "<hr>\n")))))

(defn show-404 []
  (-> (empty-body)
      (str/replace #"EMPTY"
                   (str "<h1>Error response</h1>\n"
                        "<p>Error code 404.\n"
                        "<p>Message: File not found.\n"
                        "<p>Error code explanation:"
                        "404 = Nothing matches the given URI.\n"))))

(defn show-authentication-req []
  (-> (empty-body)
      (str/replace #"EMPTY"
                   (str "<h1>Authentication required</h1>\n"
                        "<p>Authentication required\n"))))

(defn show-file [uri directory]
  (let [PUB_DIR directory]
    (io/file (str PUB_DIR uri))))

(defn show-logs []
  (io/file "http_server.log"))

(defn generate-form [body directory]
  (let [PUB_DIR directory
        file (io/file (str PUB_DIR "/form"))]
    (spit file body)))

(defn decode-parameters [query]
  (->> (str/split query #"\&")
       (map #(java.net.URLDecoder/decode %))
       (map #(str/replace-first % "=" " = "))
       (map #(str % "\n"))
       (apply str)))
