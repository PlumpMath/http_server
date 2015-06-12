(ns http-server.handler-spec
  (:require [speclj.core :refer :all]
            [http-server.handler :refer :all]
            [http-server.core :as core])
  (:refer-clojure :exclude [send]))

(describe "Tests GET /"

  (it "should return status 200"
    (should-contain "200"
                    (-> "GET / HTTP/1.1"
                        (core/adaptor "" "")
                        handler
                        :status))))

(run-specs)
