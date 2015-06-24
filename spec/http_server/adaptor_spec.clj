(ns http-server.adaptor-spec
  (:require [speclj.core :refer :all]
            [http-server.adaptor :refer :all]))

(describe "http-server.adaptor"

  (describe "Adaptor"

    (it "returns a GET method"
      (should= :get (:method (adaptor "GET / HTTP/1.1"
                                      ["Host: localhost:5000"]
                                      "This part not read"))))

    (it "returns an UNKNOWN method"
      (should= :unknown (:method (adaptor "GETX / HTTP/1.1"
                                          ["Host: localhost:5000"]
                                          "This part not read"))))

    (it "handles both headers and body"
      (should= (adaptor "PATCH /patch-content.txt HTTP/1.1"
                        ["If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"]
                        "patched content")
        {:method :patch,
         :uri "/patch-content.txt",
         :extension "txt",
         :query nil,
         :version "HTTP/1.1",
         :headers ["If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"],
         :body "patched content"}))))

(run-specs)
