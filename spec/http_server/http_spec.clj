(ns http-server.http-spec
  (:require [speclj.core :refer :all]
            [http-server.http :refer :all]))

(System/setProperty "PUB_DIR"
                    "/Users/robert/clojure-1.6.0/cob_spec-master/public")

(describe "http-server.http"
  
  (describe "Status codes"
    
    (it "can return a status code"
      (should= "HTTP/1.1 200 OK\n"
        (status 200)))
    
    (it "returns 400 for unknown codes"
      (should= "HTTP/1.1 400 Bad Request\n"
        (status 12345))))
  
  (describe "Decodes parameters"
    
    (it "decodes a string from the URL"
      (should= "my = apples\nare green and red\n"
        (decode-parameters "my=apples&are+green+and+red")))))

(run-specs)
