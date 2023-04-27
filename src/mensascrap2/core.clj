(ns mensascrap2.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :refer [generate-string]]
            [hickory.core :as h]
            [hickory.select :as s])
  (:gen-class))

(defn typecheck
  "Valid types: vegetarisch, vegan, Schwein, Fisch"
  [type]
  (let [t (str/join (drop-last 2 (first (str/split type #" "))))]
    (if (some (partial = t)  ["vegetarisch" "vegan" "Schwein" "Fish" "Rind" "Lamm" "Hähnchen"]) t "-")))

(defn parse-metadata [patient]
  (let [name (->> patient
                  (s/select (s/class "aw-meal-description"))
                  ((comp first :content first)))
        type (->> patient
                  (s/select (s/and (s/tag "span")))
                  ((comp first :content first))
                  (typecheck))
        price (->> patient
                   (s/select (s/class "aw-meal-price"))
                   (str)
                   (re-find #"\d\,\d\d")
                   (#(str/replace % "," ".")))]
    #_(snipe "/mensa-moltke/montag.html")
    {:name name :type type :price price}))

(defn snipe [endpoint]
  (->> endpoint
       (str "https://www.imensa.de/karlsruhe")
       (client/get)
       (:body)
       (h/parse)
       (h/as-hickory)
       (s/select (s/class "aw-meal-category"))
       (map parse-metadata)))

(defn buildedn []
  {:head {:api-version "v2.1"
          :last-update (.toString (java.time.LocalDateTime/now))
          :source "www.imensa.de"}
   :body {:Erzbergerstraße {:monday (snipe "/mensa-erzbergerstrasse/montag.html")
                            :tuesday (snipe "/mensa-erzbergerstrasse/dienstag.html")
                            :wednesday (snipe "/mensa-erzbergerstrasse/mittwoch.html")
                            :thursday (snipe "/mensa-erzbergerstrasse/donnerstag.html")
                            :friday (snipe "/mensa-erzbergerstrasse/freitag.html")}
          :Schloss-Gottesaue {:monay (snipe "/mensa-schloss-gottesaue/montag.html")
                              :tuesday (snipe "/mensa-schloss-gottesaue/dienstag.html")
                              :wednesday (snipe "/mensa-schloss-gottesaue/mittwoch.html")
                              :thursday (snipe "/mensa-schloss-gottesaue/donnerstag.html")
                              :friday (snipe "/mensa-schloss-gottesaue/freitag.html")}
          :Cafetaria-Moltkestraße {:monay (snipe "/cafeteria-moltkestrasse-30/montag.html")
                                   :tuesday (snipe "/cafeteria-moltkestrasse-30/dienstag.html")
                                   :wednesday (snipe "/cafeteria-moltkestrasse-30/mittwoch.html")
                                   :thursday (snipe "/cafeteria-moltkestrasse-30/donnerstag.html")
                                   :friday (snipe "/cafeteria-moltkestrasse-30/freitag.html")}
          :Mensa-Moltke {:monay (snipe "/mensa-moltke/montag.html")
                         :tuesday (snipe "/mensa-moltke/dienstag.html")
                         :wednesday (snipe "/mensa-moltke/mittwoch.html")
                         :thursday (snipe "/mensa-moltke/donnerstag.html")
                         :friday (snipe "/mensa-moltke/freitag.html")}
          :Mensa-Adenauerring {:monay (snipe "/mensa-am-adenauerring/montag.html")
                               :tuesday (snipe "/mensa-am-adenauerring/dienstag.html")
                               :wednesday (snipe "/mensa-am-adenauerring/mittwoch.html")
                               :thursday (snipe "/mensa-am-adenauerring/donnerstag.html")
                               :friday (snipe "/mensa-am-adenauerring/freitag.html")}}})

(defn -main []
  (println (generate-string (buildedn))))

#_(buildedn)
