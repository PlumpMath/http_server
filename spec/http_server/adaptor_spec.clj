(ns http-server.adaptor-spec
  (:require [speclj.core :refer :all]
            [http-server.adaptor :refer :all]))

(describe "Adaptor"

  (it "returns a GET method"
    (should= :get (:method (adaptor "GET / HTTP/1.1"
                                    '("Host: localhost:5000")
                                    "This part not read"))))

  (it "returns an UNKNOWN method"
    (should= :unknown (:method (adaptor "GETX / HTTP/1.1"
                                        '("Host: localhost:5000")
                                        "This part not read"))))

  (it "handles both headers and body"
    (should= (adaptor "PATCH /patch-content.txt HTTP/1.1"
                      '("If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec" "Content-Length: 15" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate")
                      "patched content")
      {:method :patch,
       :uri "/patch-content.txt",
       :extension "txt",
       :query nil,
       :version "HTTP/1.1",
       :headers '("If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec" "Content-Length: 15" "Host: localhost:5000" "Connection: Keep-Alive" "User-Agent: Apache-HttpClient/4.3.5 (java 1.5)" "Accept-Encoding: gzip,deflate"),
       :body "patched content"})))

(run-specs)
