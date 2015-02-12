(ns chatter.core
  (:require [markov-text.core :as mkt]
            [clojurewerkz.neocons.rest :as nr]
            [clojure.tools.cli :refer [parse-opts]]
            [twitter-streaming-client.core :as client]
            [twitter.oauth :as oauth]
            [clojure.tools.logging :as log]
            [twitter.api.streaming :as tas])
  (:gen-class))

(def ngram-size 3)

(defn connect
  [uri]
  (nr/connect uri))

(defn mentions-stream
  [creds]
  (client/create-twitter-stream tas/user-stream
                                :oauth-creds creds :params {:with "user"}))

(defn- reply-to-mentions [mentions]
  )

(defn- handle-user-stream
  [stream]
  (let [mentions (:tweet stream)]
    (reply-to-mentions mentions)
    ))

(def cli-opts
  [["-c" "--config" "config file"
    :default "chatter-config.yml"]])

(defn do-every
  [ms callback]
  (loop []
    (do
      (Thread/sleep ms)
      (try (callback)
           (catch Exception e (log/error e (str "caught exception: " (.getMessage e))))))
    (recur)))

(defn- read-config
  [path]
  {})

(defn -main
  "Run chatter"
  [& args]
  (let [{:keys [options]} (parse-opts args cli-opts)
        {:keys [ngram-size app-key app-secret user-token user-secret] :or {ngram-size 3}} (read-config (:config options))
        creds (oauth/make-oauth-creds app-key app-secret user-token user-secret)
        stream (mentions-stream creds)]
    (client/start-twitter-stream stream)
    (future (log/debug "STARTING USER STREAM")
            (do-every 60500 #(handle-user-stream stream)))))
