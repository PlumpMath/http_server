(ns http-server.log)
  ;; (:require [clojure.java.io :as io]
  ;;           [clojure.string :as str]))

(defn log [s]
  (spit "/tmp/http_server.log" (str s "\n\n") :append true))
