(ns http-server.server-spec
  (:require [speclj.core :refer :all]
            [http-server.server :refer :all])
    (:refer-clojure :exclude [send]))

(System/setProperty "PUB_DIR"
                    "/Users/robert/clojure-1.6.0/cob_spec-master/public")

(describe "http-server.server"

  (describe "Server main function"
    (let [a (server 8888 http-server.handler/handler)]
      
      (it "the server started is an atom"
        (should= clojure.lang.Atom
          (class a))))))

(run-specs)
