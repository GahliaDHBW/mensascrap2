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

(defn request [endpoint]
  (let [response (client/get (str "https://www.imensa.de/karlsruhe" endpoint))]
    (cond (= (:status response) 200) (->> response :body h/parse h/as-hickory))))

; TODO How do we efficiently request all endpoints?
(def sample (request "/mensa-erzbergerstrasse/montag.html"))

(def relevantscope
  (->> sample
       (s/select (s/class "aw-meal-category"))))

(defn -main [& args]
  (println "Hello World"))
