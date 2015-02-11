(ns chatter.core
  (:require [markov-text.core :as mkt]
            [clojurewerkz.neocons.rest :as nr]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def ngram-size 3)

(defn connect
  [uri]
  (nr/connect uri))

(defn use-training-file
  "Train the bot with a line separated training file"
  [file conn]
  (with-open [is (io/reader file)]
    (doseq [line (line-seq is)]
      (mkt/add-line line conn ngram-size))))

(defn get-line
  [conn]
  (mkt/build-line conn))

(def cli-opts
  [["-c" "--connect" "neo4j REST URL"
    :default "http://localhost:7474/db/data/"]])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-opts)
        conn (connect (:connect options))]
    (use-training-file (first arguments) conn)
    (println (get-line conn))))
