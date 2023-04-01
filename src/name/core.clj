(ns name.core
  (:require [clojure.string :as str]
            [clj-http.lite.client :as client]
            [quil.core :as q]
            [quil.middleware :as m]
            [cheshire.core :as cheshire])
  (:gen-class))

(inc 1)

(defn -main [& args]
  (println "Hello World"))
