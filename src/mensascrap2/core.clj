(ns mensascrap2.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :refer [generate-string]]
            [hickory.core :as h]
            [hickory.select :as s]
            [java-time.api :as jt])
  (:gen-class))

(def firstest (comp first :content first))

(defn- typecheck [type]
  (let [heck (partial split-with (partial not= (first " ")))
        t (str/join (drop-last 2 (first (heck type))))]
    (if (some (partial = t)  ["vegetarisch" "vegan" "Schwein" "Fish" "Rind" "Lamm" "Hähnchen"]) t "-")))

(defn- gettype [patient]
  (->> patient
       (s/select (s/tag "span"))
       (firstest)
       (typecheck)))

(defn- getname [patient]
  (->> patient
       (s/select (s/class "aw-meal-description"))
       (firstest)))

(defn normalize-picture [a]
  (if (nil? a) "https://github.com/port19x/port19.xyz/assets/82055622/bf6ebfe7-0283-4b38-b6a7-26d2c1065762" a))

(defn getpicture [query]
  (->> query
       (#(str/replace % #" " "+"))
       (#(str "https://google.com/search?q=" %
              "=lnms&tbm=isch"))
       (client/get)
       :body
       (re-seq #"https://encrypted-tbn0.gstatic.com/images\?q=tbn:[a-zA-Z0-9-_]+")
       first
       normalize-picture))

(defn- normalize-allergies [a]
  (if (empty? a) "unkown" a))

(defn- getallergies [patient]
  (->> patient
       (s/select (s/tag "span"))
       (firstest)
       (partition-by #(= \space %))
       (map (partial remove #(or (= \  %) (= \space %))))
       (remove empty?)
       (map #(apply str %))
       (drop-while #(not= "ALLERGEN" %))
       (next)
       (interpose ", ")
       (apply str)
       (normalize-allergies)))

(defn- normalize-price [p]
  (if (nil? p)
    "0.00€"
    (str p "€")))

(defn- getprice [patient]
  (->> patient
       (s/select (s/class "aw-meal-price"))
       (str)
       (re-find #"\d\,\d\d")
       (replace {\, \.})
       (apply str)
       (normalize-price)))

(defn- parse-metadata [patient]
  {:name (getname patient) :type (gettype patient) :price (getprice patient) :allergies (getallergies patient) :picture (getpicture (getname patient))})

(defn- snipe [endpoint]
  (->> endpoint
       (str "https://www.imensa.de/karlsruhe")
       (client/get)
       (:body)
       (h/parse)
       (h/as-hickory)
       (s/select (s/class "aw-meal-category"))))

(defn- buildedn []
  {:head {:api-version "v2.2"
          :last-update (jt/format "YY.MM.dd-HH:mm" (jt/local-date-time)) ; e.g. 23.05.23-09:37
          :source "www.imensa.de"}
   :body {:Erzbergerstraße {:monday (map parse-metadata (snipe "/mensa-erzbergerstrasse/montag.html"))
                            :tuesday (map parse-metadata (snipe "/mensa-erzbergerstrasse/dienstag.html"))
                            :wednesday (map parse-metadata (snipe "/mensa-erzbergerstrasse/mittwoch.html"))
                            :thursday (map parse-metadata (snipe "/mensa-erzbergerstrasse/donnerstag.html"))
                            :friday (map parse-metadata (snipe "/mensa-erzbergerstrasse/freitag.html"))}
          :Schloss-Gottesaue {:monday (map parse-metadata (snipe "/mensa-schloss-gottesaue/montag.html"))
                              :tuesday (map parse-metadata (snipe "/mensa-schloss-gottesaue/dienstag.html"))
                              :wednesday (map parse-metadata (snipe "/mensa-schloss-gottesaue/mittwoch.html"))
                              :thursday (map parse-metadata (snipe "/mensa-schloss-gottesaue/donnerstag.html"))
                              :friday (map parse-metadata (snipe "/mensa-schloss-gottesaue/freitag.html"))}
          :Cafetaria-Moltkestraße {:monday (map parse-metadata (snipe "/cafeteria-moltkestrasse-30/montag.html"))
                                   :tuesday (map parse-metadata (snipe "/cafeteria-moltkestrasse-30/dienstag.html"))
                                   :wednesday (map parse-metadata (snipe "/cafeteria-moltkestrasse-30/mittwoch.html"))
                                   :thursday (map parse-metadata (snipe "/cafeteria-moltkestrasse-30/donnerstag.html"))
                                   :friday (map parse-metadata (snipe "/cafeteria-moltkestrasse-30/freitag.html"))}
          :Mensa-Moltke {:monday (map parse-metadata (snipe "/mensa-moltke/montag.html"))
                         :tuesday (map parse-metadata (snipe "/mensa-moltke/dienstag.html"))
                         :wednesday (map parse-metadata (snipe "/mensa-moltke/mittwoch.html"))
                         :thursday (map parse-metadata (snipe "/mensa-moltke/donnerstag.html"))
                         :friday (map parse-metadata (snipe "/mensa-moltke/freitag.html"))}
          :Mensa-Adenauerring {:monday (map parse-metadata (snipe "/mensa-am-adenauerring/montag.html"))
                               :tuesday (map parse-metadata (snipe "/mensa-am-adenauerring/dienstag.html"))
                               :wednesday (map parse-metadata (snipe "/mensa-am-adenauerring/mittwoch.html"))
                               :thursday (map parse-metadata (snipe "/mensa-am-adenauerring/donnerstag.html"))
                               :friday (map parse-metadata (snipe "/mensa-am-adenauerring/freitag.html"))}}})

(defn -main []
  (println (generate-string (buildedn))))

(comment
  (-main)
  (buildedn)
  (def sample (first (snipe "/mensa-erzbergerstrasse/montag.html")))
  (parse-metadata sample)
  (gettype sample)
  (getallergies sample)
  (getprice sample)
  (getname sample))
