(ns http-server.main-spec
  (:require [speclj.core :refer :all]
            [clojure.java.shell :as sh]
            [http-server.main :refer :all]))

(System/setProperty "PUB_DIR" "/Users/robert/clojure-1.6.0/cob_spec-master/public")

(do (sh/sh "rm" "-rf" "/tmp/view-tests")
    (sh/sh "mkdir" "/tmp/view-tests")
    (spit "/tmp/view-tests/file01.txt" "file 01 contents")
    (spit "/tmp/view-tests/file02.txt" "file 02 contents")
    (spit "/tmp/view-tests/file03.txt" "file 03 contents"))

(run-specs)
