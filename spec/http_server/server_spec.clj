(ns http-server.server-spec
  (:require [speclj.core :refer :all]
            [http-server.server :refer :all])
    (:refer-clojure :exclude [send]))

(describe "Server main function"
  (let [a (server 8888 http-server.handler/handler "/Users/robert/clojure-1.6.0/cob_spec-master/public")]
  
  (it "the server started is an atom"
    (should= clojure.lang.Atom
      (class a)))))

(run-specs)
