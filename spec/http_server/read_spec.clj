(ns http-server.read-spec
  (:require [speclj.core :refer :all]
            [http-server.read :refer :all]))

(describe "http-server.read"

(describe "Read functions"

  (it "handles both CR and LF"
    (should= "foobar"
      (read-line-crlf (java.io.StringReader. "foobar\r\nohai\r\n"))))

  (it "handles LF before CR"
    (should= "foobar"
      (read-line-crlf (java.io.StringReader. "foobar\nohai\r\n"))))

  (it "only LF not OK"
    (should-throw
     (read-line-crlf (java.io.StringReader. "foobar\rohai\r\n"))))

  (it "needs either CR or LF"
    (should-throw
     (read-line-crlf (java.io.StringReader. "foobar"))))

  (it "reads messages until double \\n"
    (should= ["foo" "bar"]
      (read-msgs (java.io.StringReader. "foo\nbar\n\n") [])))

  (it "throws exception for single \\n"
    (should-throw
     (read-msgs (java.io.StringReader. "foo\nbar\n") [])))

  (it "throws exception for no \\n"
    (should-throw
     (read-msgs (java.io.StringReader. "foo") [])))

  (let [result (spit "tmp/test.txt" "foo")]
    (it "reads to vector of bytes while stream is ready"
      (should= [102 111 111]
        (read-while-ready (clojure.java.io/reader "tmp/test.txt") []))))))

(run-specs)
