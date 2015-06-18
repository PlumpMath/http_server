(ns http-server.handler
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [http-server.files :as files]
            [http-server.view :as view]
            [http-server.http :as http]))

(defn empty-response []
  {:status ""
   :header (http/header :empty),
   :body ""})

(defn range-in-header? [headers]
  (when headers (re-find #"Range:" (first headers))))

(defn patch-in-header? [headers]
  (when headers (re-find #"If-Match:" (first headers))))

(defn auth-in-header? [headers]
  (when headers (re-find #"Authorization:" (first headers))))

(defn correct-authentication? [headers]
  (let [userid-password "admin:hunter2"
        b64-string (if (auth-in-header? headers)
                     (-> headers first (str/split #" ") last))]
    (if b64-string (= userid-password (files/base64-decode b64-string)))))

(defn file-in-directory? [uri directory]
  (let [PUB_DIR directory
        file (subs uri 1)
        files (rest (file-seq (io/file PUB_DIR)))
        filenames (map #(.getName %) files)]
    (some #{file} filenames)))

(defn get-handler [{:keys [uri extension query headers] :as request} directory]
  (cond query
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :body (http/decode-parameters query)))
        (= uri "/")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (http/header :html)
                   :body (view/show-index directory)))
        (= uri "/form")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (http/header :html)
                   :body (files/show-file uri directory)))
        (= uri "/logs")
        (if (correct-authentication? headers)
          (-> (empty-response)
              (assoc :status (http/status 200)
                     :header (http/header :text)
                     :body (files/show-logs)))
          (-> (empty-response)
              (assoc :status (http/status 401)
                     :header (http/header :html)
                     :body (view/show-authentication-req))))
        (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (-> (http/header :empty)
                               http/add-options-to-header)))
        (= uri "/redirect")
        (-> (empty-response)
            (assoc :status (http/status 302)
                   :header (http/add-redirect-to-header (http/header :empty)
                                                      headers)))
        (range-in-header? headers)
        (-> (empty-response)
            (assoc :status (http/status 206)
                   :header (http/header :text) ;; Only handles range for text
                   :body (files/file-range uri headers directory)))
        (files/patched-file? uri)
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (http/header :text)
                   :body (files/show-patched-file uri)))
        (some #(= extension %) ["jpeg" "gif" "png"])
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (http/image-header extension)
                   :body (files/show-file uri directory)))
        (= extension "txt")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (http/header :text)
                   :body (files/show-file uri directory)))
        (= extension "html")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (http/header :html)
                   :body (files/show-file uri directory)))
        (file-in-directory? uri directory)
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (http/header :text) ;; Assumed to be text if no ext
                   :body (files/show-file uri directory)))
        :else
        (-> (empty-response)
            (assoc :status (http/status 404)
                   :header (http/header :404)
                   :body (view/show-404)))))

(defn put-handler [{:keys [uri body] :as request} directory]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (-> (http/header :empty)
                               http/add-options-to-header)))
        (= uri "/form")
        (do (files/generate-form body directory)
            (-> (empty-response)
                (assoc :status (http/status 200))))
        :else
        (-> (empty-response)
            (assoc :status (http/status 405)))))

(defn post-handler [{:keys [uri body] :as request} directory]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (-> (http/header :html)
                               http/add-options-to-header)))
        (= uri "/form")
        (do (files/generate-form body directory)
            (-> (empty-response)
                (assoc :status (http/status 200))))
        :else
        (-> (empty-response)
            (assoc :status (http/status 405)))))

(defn patch-handler [{:keys [uri headers body] :as request} directory]
  (files/add-patch uri headers body)
  (-> (empty-response)
      (assoc :status (http/status 204))))

(defn delete-handler [{:keys [uri] :as request} directory]
  (cond (= uri "/form")
        (do (files/empty-form directory)
            (-> (empty-response)
                (assoc :status (http/status 200))))
        :else (empty-response)))

(defn head-handler [{:keys [uri] :as request}]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (-> (http/header :empty)
                               http/add-options-to-header)))
        :else
        (-> (empty-response)
            (assoc :status (http/status 200)))))

(defn options-handler [{:keys [uri] :as request}]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (http/status 200)
                   :header (-> (http/header :empty)
                               http/add-options-to-header)))
        :else (empty-response)))

(defn handler [{:keys [method] :as request} directory]
  (case method
    :get (get-handler request directory)
    :put (put-handler request directory)
    :post (post-handler request directory)
    :patch (patch-handler request directory)
    :delete (delete-handler request directory)
    :head (head-handler request)
    :options (options-handler request)
    (empty-response)))
