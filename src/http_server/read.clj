(ns http-server.read
  (:require [clojure.string :as str]))

(def cr  0x0d)
(def lf  0x0a)
(defn- cr? [c] (= c cr))
(defn- lf? [c] (= c lf))

(defn read-line-crlf [rdr]
  (loop [line []
         c (.read rdr)]
    (when (< c 0)
      (throw (Exception. (str "Error reading line: "
                              "EOF reached before CR/LF sequence"))))
    (cond (lf? c) (apply str line)
          (cr? c) (let [next (.read rdr)]
                    (if (lf? next)
                      (apply str line)
                      (throw (Exception. "Error reading line: Missing LF"))))
          :else (recur (conj line (char c))
                       (.read rdr)))))

(defn read-msgs [rdr res]
  (let [msg (read-line-crlf rdr)]
    (if (empty? msg) res
        (read-msgs rdr (conj res msg)))))

(defn read-while-ready [rdr res]
  (if-not (.ready rdr)
    res
    (read-while-ready rdr (conj res (.read rdr)))))

(defn read-body [rdr]
  (->> (read-while-ready rdr [])
       (map char)
       (apply str)))
