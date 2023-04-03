(ns mensascrap2.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [hickory.core :as h]
            [hickory.select :as s]
            [clojure.math.combinatorics :as combo])
  (:gen-class))

(def endpoints (let [mensas #{"/mensa-erzbergerstrasse" "/mensa-schloss-gottesaue" "/cafeteria-moltkestrasse-30" "/mensa-moltke" "/mensa-am-adenauerring"}
                     days #{"/montag" "/dienstag" "/mittwoch" "/donnerstag" "/freitag"}
                     extensions #{".html"}
                     combinations (combo/cartesian-product mensas days extensions)]
                 (map (partial apply str) combinations)))

; How do we deal with request failure?
(def primary "https://www.imensa.de/karlsruhe")
(defn request
  [endpoint]
  (let [response (client/get (str primary endpoint))]
    (cond (= (:status response) 200) {:source primary :resp (-> (:body response) h/parse h/as-hickory)})))

(def sample (request "/mensa-erzbergerstrasse/montag.html"))

; TODO retarget to imensa
; bbyt select examples
;(s/select (s/attr "dir"))
;(s/select (s/class "length"))
;(s/select (s/descendant (s/child (s/and (s/attr "href") (s/attr "style")))))
; how do I select multiple times / how do I make selected stuff a document again?

; HACK Old code lol
(def subject (->> "sample.html" slurp h/parse h/as-hickory))
(def relevantscope
  (->> subject
       (s/select (s/tag "table"))
       first
       :content))

(defn -main [& args]
  (println "Hello World"))
