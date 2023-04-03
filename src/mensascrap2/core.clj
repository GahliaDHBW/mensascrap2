(ns mensascrap2.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [hickory.core :as h]
            [hickory.select :as s])
  (:gen-class))

(def subject (->> "sample.html" slurp h/parse h/as-hickory))

; bbyt select examples
;(s/select (s/attr "dir"))
;(s/select (s/class "length"))
;(s/select (s/descendant (s/child (s/and (s/attr "href") (s/attr "style")))))
; how do I select multiple times / how do I make selected stuff a document again?
(def relevantscope
  (->> subject
       (s/select (s/tag "table"))
       first
       :content))

(defn -main [& args]
  (println "Hello World"))
