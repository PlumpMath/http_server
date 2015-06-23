(ns http-server.handler-spec
  (:require [speclj.core :refer :all]
            [http-server.handler :refer :all])
  (:refer-clojure :exclude [send]))

(describe "Query functions"

  (it "range-in-header?"
    (should (range-in-header? '("Range: bytes=0-4" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")))

    (should (range-in-header? '("Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Range: bytes=0-4" "Accept-Encoding: gzip,deflate")))

    (should-not (range-in-header? '("Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate"))))

  (it "patch-in-header?"
    (should (patch-in-header? '("If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec" "Content-Length: 15" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")))

    (should (patch-in-header? '("Content-Length: 15" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec" "Accept-Encoding: gzip,deflate")))

    (should-not (patch-in-header? '("Content-Length: 15" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate"))))
    
  (it "auth-in-header?"
    (should (auth-in-header? '("Authorization: Basic YWRtaW46aHVudGVyMg==" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")))

    (should (auth-in-header? '("Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Authorization: Basic YWRtaW46aHVudGVyMg==" "Accept-Encoding: gzip,deflate")))

    (should-not (auth-in-header? '("Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate"))))

  (it "correct-authentication?"
    (should (correct-authentication? '("Authorization: Basic YWRtaW46aHVudGVyMg==" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")))

    (should-not (correct-authentication? '("Authorization: Basic xxxxx" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate"))))

  (it "file-in-directory?"
    (should (file-in-directory? "/file1" "/Users/robert/clojure-1.6.0/cob_spec-master/public"))

    (should-not (file-in-directory? "/file4" "/Users/robert/clojure-1.6.0/cob_spec-master/public"))))


(describe "Get handler"

  (it "should only show logs if correct password"
    (should= "HTTP/1.1 200 OK\n"
      (-> (handler {:method :get,
                    :uri "/logs",
                    :headers '("Authorization: Basic YWRtaW46aHVudGVyMg==" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")}
                   "/Users/robert/clojure-1.6.0/cob_spec-master/public")
          (get :status))))

  (it "should only show logs if correct password"
    (should= "HTTP/1.1 401 Unauthorized\n"
      (-> (handler {:method :get,
                    :uri "/logs",
                    :headers '("Authorization: Basic XXXXX" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")}
                   "/Users/robert/clojure-1.6.0/cob_spec-master/public")
          (get :status))))

  (it "handles range requests"
    (should= "HTTP/1.1 206 Partial Content\n"
      (-> (handler {:method :get,
                    :uri "/partial_content.txt",
                    :headers '("Range: bytes=0-4" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")}
                   "/Users/robert/clojure-1.6.0/cob_spec-master/public")
          (get :status)))

    (should= "This is"
      (->> (:body (handler {:method :get,
                            :uri "/partial_content.txt",
                            :headers '("Range: bytes=0-6" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")}
                           "/Users/robert/clojure-1.6.0/cob_spec-master/public"))
           (map char)
           (apply str)))

    (should= "ll a 206.\n"
      (->> (:body (handler {:method :get,
                            :uri "/partial_content.txt",
                            :headers '("Range: bytes=-10" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")}
                           "/Users/robert/clojure-1.6.0/cob_spec-master/public"))
           (map char)
           (apply str)))
                    
    (should= "a 206.\n"
      (->> (:body (handler {:method :get,
                            :uri "/partial_content.txt",
                            :headers '("Range: bytes=70-" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")}
                           "/Users/robert/clojure-1.6.0/cob_spec-master/public"))
           (map char)
           (apply str)))

    (should= ""
      (->> (:body (handler {:method :get,
                            :uri "/partial_content.txt",
                            :headers '("Range: bytes=20-10" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")}
                           "/Users/robert/clojure-1.6.0/cob_spec-master/public"))
           (map char)
           (apply str)))))


(describe "Post handler"
  (let [result (spit (clojure.java.io/file "/tmp/form") "" :append false)
        result (http-server.files/generate-form "Test123")]
  
    (it "posts go to /tmp/form"
      (should (re-find #"Test123" (slurp (clojure.java.io/file "/tmp/form")))))))
    
(run-specs)










