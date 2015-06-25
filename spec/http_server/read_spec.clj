(ns http-server.read-spec
  (:require [speclj.core :refer :all]
            [http-server.read :refer :all]))

(defn setup-tmp-files [tmpdir]
  (spit (str tmpdir "/test.txt") "foo"))

(defn get-file-from-tmp [file]
  (str (System/getProperty "TMP_DIR")
       "/" file))

(describe "http-server.read"

  (around [it]
          (System/setProperty "TMP_DIR" "tmp")
          (setup-tmp-files (System/getProperty "TMP_DIR"))
          (it))
  
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

    (it "reads to vector of bytes while stream is ready"
      (should= [102 111 111]
        (read-while-ready
         (clojure.java.io/reader (get-file-from-tmp "test.txt")) [])))))

(run-specs)
