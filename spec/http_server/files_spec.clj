(ns http-server.files-spec
  (:require [speclj.core :refer :all]
            [clojure.java.shell :as sh]
            [http-server.files :refer :all]))

(defn setup-tmp-files [tmpdir]
  (spit (str tmpdir "/test.txt") "foo")
  (spit (str tmpdir "/patches.edn") ""))

(defn get-file-from-tmp [file]
  (str (System/getProperty "TMP_DIR")
       "/" file))

(defn get-file-from-pub [file]
  (str (System/getProperty "PUB_DIR")
       "/" file))

(defn get-real-file-from-pub [file]
  (clojure.java.io/file (str (System/getProperty "PUB_DIR")
                             "/" file)))

(describe "http.server-files"
  (around [it]
          (System/setProperty "PUB_DIR" "public")
          (System/setProperty "TMP_DIR" "tmp")
          (setup-tmp-files (System/getProperty "TMP_DIR"))
          (add-patch "/file1"
                       ["If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"]
                       "foobar")
          (it))
  
  (describe "Reads a file to a byte array"
    
    (it "reads a file to a byte array"
      (should= [\f \o \o]
        (->> (file-to-byte-array
              (clojure.java.io/file
               (get-file-from-tmp "test.txt")))
             (map char)))))

  (describe "Ranges from files"

    (it "can provide the range of a file as a byte array"
      (should= [\f \i \l \e]
        (->> (file-range (get-real-file-from-pub "file1")
                         ["Range: bytes=0-3"])
             (map char)))

      (should= [\c \o \n \t \e \n \t \s]
          (->> (file-range (get-real-file-from-pub "file1")
                           ["Range: bytes=6-"])
             (map char)))
      
      (should= [\e \n \t \s]
        (->> (file-range (get-real-file-from-pub "file1")
                         ["Range: bytes=-4"])
             (map char)))))

  (describe "Ranges from strings"

    (it "tests string content with range"
      (should= "GET /"
        (->>
         (file-range "GET / HTTP/1.1"
                     ["Range: bytes=0-4"])
         (map char)
         (apply str)))))
  
  (describe "Bytes from files"

    (it "can return the range of a file as a bytes"
      (should= [\a \b \c \d]
        (bytes-range "abcdef" 0 3))

      (should= [\e \f]
        (bytes-range "abcdef" 4 5))))

  (describe "Tests for the get-range function"

    (it "returns a range for 0-3 (non-inclusive with 4) with a file of size 6"
      (should= [0 3]
        (get-range "0-3" 6)))

    (it "returns a range for 4-6 expressed as 4- with a file of size 6"
      (should= [4 5]
        (get-range "4-" 6)))

    (it "returns a range for 4-6 expressed as -3 with a file of size 6"
      (should= [3 5]
        (get-range "-3" 6))))

  (describe "Tests base64 decode"

    (it "should decode a message"
      (should= "base64 decoder"
        (base64-decode "YmFzZTY0IGRlY29kZXI=")))

    (it "converts b64 to ascii codes"
      (should= [98 97 115 101 54 52 32 100 101 99 111 100 101 114]
        (base64-to-bytes "YmFzZTY0IGRlY29kZXI="))))

  (describe "Tests for patch functionality"
      
    (it "demonstrates what contents of file1 is before patch"
      (should= "file1 contents"
        (slurp (clojure.java.io/file (get-file-from-pub "file1")))))
    
    (it "should not change a file for patch to work"
      (should= "file1 contents"
        (slurp (clojure.java.io/file (get-file-from-pub "file1")))))
    
    (it "shows patched content"
      (should= "foobar" (show-patched-file "/file1"))))

  (describe "Tests for form file functionality"

    (it "no previous entries"
      (let [result (generate-empty-form)]
        (should-not-contain "foobar" (slurp
                                      (get-file-from-tmp "form")))))

    (it "enters the content of body as the form entry"
      (let [result (generate-form "foobar")]
        (should-contain "foobar" (slurp
                                  (get-file-from-tmp "form"))))))

  (describe "Show logs"

    (it "shows logs"
      (should-contain "HTTP/1.1"
        (slurp (get-file-from-tmp "http_server.log"))))))

(run-specs)
