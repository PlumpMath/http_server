(ns http-server.adaptor
  (:require [clojure.string :as str]))

(defn get-filetype [uri]
  (when (re-find #"\." uri)
    (last (str/split uri #"\."))))

(defn adaptor [msg headers body]
  (let [[method uri version] (str/split msg #" ")
        [uri query] (if uri (-> uri (str/split #"\?")) [nil nil])
        extension (get-filetype uri)]
    {:method (condp = method
               "GET" :get
               "HEAD" :head
               "POST" :post
               "PUT" :put
               "DELETE" :delete
               "OPTIONS" :options
               "TRACE" :trace
               "CONNECT" :connect
               "PATCH" :patch
               :unknown),
     :uri uri,
     :extension extension,
     :query query,
     :version version,
     :headers headers,
     :body body}))
