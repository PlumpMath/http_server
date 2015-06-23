(ns http-server.view
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn empty-body []
  (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n"
       "<html>\n"
       "<body>\n"
       "EMPTY"
       "</body>\n"
       "</html>\n"))

(defn show-index []
  (let [PUB_DIR (System/getProperty "PUB_DIR")
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
