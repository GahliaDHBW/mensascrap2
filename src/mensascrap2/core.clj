(ns mensascrap2.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :refer [generate-string]]
            [hickory.core :as h]
            [hickory.select :as s])
  (:gen-class))

(defn request [endpoint]
  (let [response (client/get (str "https://www.imensa.de/karlsruhe" endpoint))]
    (if (= (:status response) 200) (->> response :body h/parse h/as-hickory) (:status response))))

(def splice-div (comp (partial first) (partial :content) (partial first)))

(defn typecheck
  "Valid types: vegetarisch, vegan, Schwein, Fisch"
  [type]
  (let [t (apply str (drop-last 2 (first (str/split type #" "))))]
    (if (or (= t "vegetarisch") (= t "vegan") (= t "Schwein") (= t "Fisch")) t "-")))

(defn parse-metadata [patient]
  (let [name (->> patient
                  (s/select (s/class "aw-meal-description"))
                  (splice-div))
        type (->> patient
                  (s/select (s/and (s/tag "span")))
                  (splice-div)
                  (typecheck))
        price (->> patient
                   (s/select (s/class "aw-meal-price"))
                   (splice-div)
                   (drop-last 2)
                   (apply str)
                   (#(str/replace % "," ".")))]
    {:name name :type type :price price}))

(defn snipe [endpoint]
  (->> endpoint
       (request)
       (s/select (s/class "aw-meal-category"))
       (map parse-metadata)))

(defn -main [& args]
  (println (generate-string
            {:head {:api-version "v2"
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
                                         :friday (snipe "/mensa-am-adenauerring/freitag.html")}}})))
