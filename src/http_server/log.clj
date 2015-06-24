(ns http-server.log)

(defn log [s]
  (spit "/tmp/http_server.log" (str s "\n\n") :append true))
