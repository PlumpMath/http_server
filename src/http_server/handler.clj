(ns http-server.handler
  (:require [clojure.string :as str]
            [http-server.files :as files]
            [http-server.operations :as op]))

(defn empty-response []
  {:status "",
   :header (op/header :empty),
   :body ""})

(defn range-in-header? [headers]
  (when headers (re-find #"Range:" (first headers))))

(defn auth-in-header? [headers]
  (when headers (re-find #"Authorization:" (first headers))))

(defn correct-authentication? [headers]
  (let [userid-password "admin:hunter2"
        b64-string (if (auth-in-header? headers)
                     (-> headers first (str/split #" ") last))]
    (if b64-string (= userid-password (files/base64-decode b64-string)))))

(defn get-handler [{:keys [uri extension query headers] :as request}]
  (cond query
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :body (op/decode-parameters query)))
        (= uri "/")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (op/header :html)
                   :body (op/show-index)))
        (= uri "/form")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (op/header :text)
                   :body (op/show-file uri)))
        (= uri "/logs")
        (if (correct-authentication? headers)
          (-> (empty-response)
              (assoc :status (op/status 200)
                     :header (op/header :text)
                     :body (op/show-logs)))
          (-> (empty-response)
              (assoc :status (op/status 401)
                     :header (op/header :html)
                     :body (op/show-authentication-req))))
        (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (-> (op/header :empty)
                               op/add-options-to-header)))
        (= uri "/redirect")
        (-> (empty-response)
            (assoc :status (op/status 302)
                   :header (op/add-redirect-to-header (op/header :empty)
                                                      headers)))
        (range-in-header? headers)
        (-> (empty-response)
            (assoc :status (op/status 206)
                   :header (op/header :text) ;; Only handles range for text
                   :body (files/file-range uri headers)))
        (some #(= extension %) ["jpeg" "gif" "png"])
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (op/image-header extension)
                   :body (op/show-file uri)))
        (= extension "txt")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (op/header :text)
                   :body (op/show-file uri)))
        (some #(= uri %) ["/file1" "/file2"])
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (op/header :text) ;; Assumed to be text if no ext
                   :body (op/show-file uri)))
        :else
        (-> (empty-response)
            (assoc :status (op/status 404)
                   :header (op/header :404)
                   :body (op/show-404)))))

(defn put-handler [{:keys [uri body] :as request}]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (-> (op/header :empty)
                               op/add-options-to-header)))
        (= uri "/form")
        (do (op/generate-form body)
            (-> (empty-response)
                (assoc :status (op/status 200))))
        :else
        (-> (empty-response)
            (assoc :status (op/status 405)))))

(defn post-handler [{:keys [uri body] :as request}]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (-> (op/header :empty)
                               op/add-options-to-header)))
        (= uri "/form")
        (do (op/generate-form body)
            (-> (empty-response)
                (assoc :status (op/status 200))))
        :else
        (-> (empty-response)
            (assoc :status (op/status 405)))))

(defn patch-handler [{:keys [uri body] :as request}]
  (files/generate-file uri body)
  (-> (empty-response)
      (assoc :status (op/status 204))))

(defn delete-handler [{:keys [uri] :as request}]
  (cond (= uri "/form")
        (do (op/generate-form "")
            (-> (empty-response)
                (assoc :status (op/status 200))))
        :else (empty-response)))

(defn head-handler [{:keys [uri] :as request}]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (-> (op/header :empty)
                               op/add-options-to-header)))
        :else
        (-> (empty-response)
            (assoc :status (op/status 200)))))

(defn options-handler [{:keys [uri] :as request}]
  (cond (= uri "/method_options")
        (-> (empty-response)
            (assoc :status (op/status 200)
                   :header (-> (op/header :empty)
                               op/add-options-to-header)))
        :else (empty-response)))

(defn handler [{:keys [method] :as request}]
  (case method
    :get (get-handler request)
    :put (put-handler request)
    :post (post-handler request)
    :patch (patch-handler request)
    :delete (delete-handler request)
    :head (head-handler request)
    :options (options-handler request)
    (empty-response)))
