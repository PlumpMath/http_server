(ns http-server.view-spec
  (:require [speclj.core :refer :all]
            [clojure.java.shell :as sh]
            [http-server.view :refer :all]))

(defn setup-tmp-files [tmpdir]
  (sh/sh "mkdir" "-p" (str tmpdir "/view-tests"))
  (spit (str tmpdir "/view-tests/file01.txt") "file 01 contents")
  (spit (str tmpdir "/view-tests/file02.txt") "file 02 contents")
  (spit (str tmpdir "/view-tests/file03.txt") "file 03 contents"))

(describe "http-server.view"
  
  (around [it]
          (System/setProperty "TMP_DIR" "tmp")
          (setup-tmp-files (System/getProperty "TMP_DIR"))
          (System/setProperty "PUB_DIR"
                              (str (System/getProperty "TMP_DIR")
                                   "/view-tests"))
          (it))
  
  (describe "show-index tests"
    
    (it "shows index of a directory"
      (should= (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"
                    "\n"
                    "<html>\n<body>\n"
                    "<h2>Directory listing for /</h2><hr>\n"
                    "<ul>\n"
                    "<li><a href=\"/file01.txt\">file01.txt</a>\n"
                    "<li><a href=\"/file02.txt\">file02.txt</a>\n"
                    "<li><a href=\"/file03.txt\">file03.txt</a>\n"
                    "</ul>\n"
                    "<hr>\n</body>\n</html>\n")
        (show-index)))))

(run-specs)
