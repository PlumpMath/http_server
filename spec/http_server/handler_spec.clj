(ns http-server.handler-spec
  (:require [speclj.core :refer :all]
            [http-server.handler :refer :all])
  (:refer-clojure :exclude [send]))

(defn get-file-from-tmp [file]
  (str (System/getProperty "TMP_DIR")
       "/" file))

(describe "http-server.handler"
  (around [it]
          (System/setProperty "PUB_DIR" "public")
          (System/setProperty "TMP_DIR" "tmp")
          (it))
  
  (describe "Query functions"
    
    (it "range-in-header?"
      (should (range-in-header? ["Range: bytes=0-4"
                                  "Host: localhost:5000"]))

      (should (range-in-header? ["Host: localhost:5000"
                                  "Range: bytes=0-4"]))

      (should-not (range-in-header? ["Host: localhost:5000"])))

    (it "patch-in-header?"
      (should (patch-in-header?
               ["If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"
                "Host: localhost:5000"]))
      
      (should (patch-in-header?
               ["Host: localhost:5000"
                "If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"]))

      (should-not (patch-in-header? ["Host: localhost:5000"])))
    
    (it "auth-in-header?"
      (should (auth-in-header? ["Authorization: Basic YWRtaW46aHVudGVyMg=="
                                 "Host: localhost:5000"]))

      (should (auth-in-header? ["Host: localhost:5000"
                                 "Authorization: Basic YWRtaW46aHVudGVyMg=="]))

      (should-not (auth-in-header? ["Host: localhost:5000"])))

    (it "correct-authentication?"
      (should (correct-authentication?
               ["Authorization: Basic YWRtaW46aHVudGVyMg=="]))

      (should-not (correct-authentication?
                   ["Authorization: Basic Zm9vYmFy"])))

    (it "file-in-directory?"
      (should (file-in-directory? "/file1"))
      
      (should-not (file-in-directory? "/file4"))))

  (describe "Get handler"

    (it "should only show logs if correct password"
      (should= "HTTP/1.1 200 OK\n"
        (:status
         (handler {:method :get,
                   :uri "/logs",
                   :headers ["Authorization: Basic YWRtaW46aHVudGVyMg=="]}))))

    (it "should only show logs if correct password"
      (should= "HTTP/1.1 401 Unauthorized\n"
        (:status (handler {:method :get,
                           :uri "/logs",
                           :headers ["Authorization: Basic Zm9vYmFy"]}))))

    (it "handles range requests"
      (should= "HTTP/1.1 206 Partial Content\n"
        (:status (handler {:method :get,
                           :uri "/partial_content.txt",
                           :headers ["Range: bytes=0-4"]})))

      (should= "This is"
        (->> (:body (handler {:method :get,
                              :uri "/partial_content.txt",
                              :headers ["Range: bytes=0-6"]}))
             (map char)
             (apply str)))

      (should= "ll a 206.\n"
        (->> (:body (handler {:method :get,
                              :uri "/partial_content.txt",
                              :headers ["Range: bytes=-10"]}))
             (map char)
             (apply str)))
      
      (should= "a 206.\n"
        (->> (:body (handler {:method :get,
                              :uri "/partial_content.txt",
                              :headers ["Range: bytes=70-"]}))
             (map char)
             (apply str)))

      (should= ""
        (->> (:body (handler {:method :get,
                              :uri "/partial_content.txt",
                              :headers ["Range: bytes=20-10"]}))
             (map char)
             (apply str)))))

  (describe "Logs functions"

    (it "returns status 200 if correct auth"
      (should= "HTTP/1.1 200 OK\n"
        (:status
         (handler {:method :get
                   :uri "/logs"
                   :headers ["Authorization: Basic YWRtaW46aHVudGVyMg=="]}))))

    (it "returns status 401 if not correct auth"
      (should= "HTTP/1.1 401 Unauthorized\n"
        (:status (handler {:method :get
                           :uri "/logs"
                           :headers ["Authorization: Basic Zm9vYmFy"]}))))

    (it "shows auth req if not correct auth"
      (should-contain "Authentication required"
                      (:body
                       (handler {:method :get
                                 :uri "/logs"
                                 :headers ["Authorization: Basic Zm9vYmFy"]})))))

  (describe "GET for files"

    (it "identifies text"
      (should-contain "text/plain"
                      (:header (handler {:method :get
                                         :uri "/text-file.txt"
                                         :extension "txt"}))))

    (it "identifies html"
      (should-contain "text/html"
                      (:header (handler {:method :get
                                         :uri "/test.html"
                                         :extension "html"}))))

    (it "identifies jpeg"
      (should-contain "image/jpeg"
                      (:header (handler {:method :get
                                         :uri "/image.jpeg"
                                         :extension "jpeg"}))))

    (it "identifies png"
      (should-contain "image/png"
                      (:header (handler {:method :get
                                         :uri "/image.png"
                                         :extension "png"}))))

    (it "identifies gif"
      (should-contain "image/gif"
                      (:header (handler {:method :get
                                         :uri "/image.gif"
                                         :extension "gif"})))))
  
  (describe "Post handler"
    (let [result (spit (clojure.java.io/file
                        (get-file-from-tmp "form"))
                       "" :append false)
          result (http-server.files/generate-form "Test123")]
      
      (it "posts go to TMP_DIR/form"
        (should
          (re-find #"Test123" (slurp (clojure.java.io/file
                                      (get-file-from-tmp "form")))))))))

(run-specs)
