(ns http-server.log
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn log [request]
  (spit "http_server.log" (str request "\n\n") :append true))
