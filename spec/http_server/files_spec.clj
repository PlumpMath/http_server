(ns http-server.files-spec
  (:require [speclj.core :refer :all]
            [http-server.files :refer :all]))

(System/setProperty "PUB_DIR"
                    "/Users/robert/clojure-1.6.0/cob_spec-master/public")

(describe "http.server-files"

  (describe "Reads a file to a byte array"
    (let [result (spit "/tmp/test.txt" "foo" :append false)]

      (it "reads a file to a byte array"
        (should= [\f \o \o]
          (->> (file-to-byte-array (clojure.java.io/file "/tmp/test.txt"))
               (map char))))))

  (describe "Ranges from files"

    (it "can provide the range of a file as a byte array"
      (should= [\f \i \l \e]
        (->> (file-range "/file1" ["Range: bytes=0-3"])
             (map char)))

      (should= [\c \o \n \t \e \n \t \s]
        (->> (file-range "/file1" ["Range: bytes=6-"])
             (map char)))
      
      (should= [\e \n \t \s]
        (->> (file-range "/file1" ["Range: bytes=-4"])
             (map char))))

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
    (let [PUB_DIR (System/getProperty "PUB_DIR")
          result (spit (clojure.java.io/file "/tmp/patches.edn") "")]

      (it "demonstrates that patch file is empty at start of this test"
        (should (empty? (slurp (clojure.java.io/file "/tmp/patches.edn")))))
      
      (it "demonstrates what contents of file1 is before patch"
        (should= "file1 contents"
          (slurp (clojure.java.io/file (str PUB_DIR "/file1")))))
      
      (it "should not change a file for patch to work"
        (let [result
              (add-patch "/file1"
                         ["If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"]
                         "foobar")]
          (should= "file1 contents"
            (slurp (clojure.java.io/file (str PUB_DIR "/file1"))))))

      (it "shows patched content"
        (let [result
              (add-patch "/file1"
                         ["If-Match: dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"]
                         "foobar")]
          (should= "foobar" (show-patched-file "/file1"))))))

  (describe "Tests for form file functionality"

    (it "no previous entries"
      (let [result (generate-empty-form)]
        (should-not-contain "foobar" (slurp "/tmp/form"))))

    (it "enters the content of body as the form entry"
      (let [result (generate-form "foobar")]
        (should-contain "foobar" (slurp "/tmp/form"))))))

(run-specs)
