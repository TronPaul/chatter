(ns chatter.core
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful]
        [twitter.api.streaming])
  (:require [clj-yaml.core :as yaml]
            [clojure.tools.cli :as cli]
            [twitter-streaming-client.core :as client]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:gen-class)
  (:import (twitter.callbacks.protocols SyncSingleCallback)))

(defn- make-mentions-stream
  [creds]
  (client/create-twitter-stream user-stream
                                :oauth-creds creds :params {:with "user"}))

(defn- irrelevant-mention?
  [user-name tweet]
  (or (empty? tweet)
      (= (get-in tweet [:user :screen_name]) user-name)
      (and (= (get-in tweet [:retweeted_status :user :screen_name]) user-name)
           (.startsWith (get-in tweet [:retweeted_status :text]) "@"))))

(defn- make-reply
  [text]
  "Test reply. Please ignore Kappa")

(defn reply-to-mention
  [creds {:keys [text id] user-name :user/screen_name}]
  (try
    (statuses-update :oauth-creds creds
                     :params {:status (str "@" name " " (make-reply text))
                              :in_reply_to_status_id id}
                     :callbacks (SyncSingleCallback. response-return-body
                                                     response-throw-error
                                                     exception-rethrow))
    (catch Exception e
      (log/error e (str "Could not reply to " user-name)))))

(defn- reply-to-mentions
  [creds user-name mentions]
  (when-let [relevant-mentions (filter (complement (partial irrelevant-mention? user-name)) mentions)]
    (doseq [rm relevant-mentions] (reply-to-mention creds rm))))

(defn- handle-mentions-stream
  [creds user-name mentions-stream]
  (let [{mentions :tweet} (client/retrieve-queues mentions-stream)]
    (reply-to-mentions creds user-name mentions)))

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
  (let [config-file (io/as-file path)]
    (with-open [is (io/input-stream config-file)]
      (yaml/parse-string is))))

(defn -main
  "Run chatter"
  [& args]
  (let [{:keys [options]} (cli/parse-opts args cli-opts)
        {:keys [ngram-size app-key app-secret user-token user-secret] :or {ngram-size 3}} (read-config (:config options))
        creds (make-oauth-creds app-key app-secret user-token user-secret)
        mentions-stream (make-mentions-stream creds)
        {user-name :screen_name} (:body (account-verify-credentials :oauth-creds creds))]
    (log/info (str "Logged in as: " user-name))
    (client/start-twitter-stream mentions-stream)
    (future (log/debug "STARTING USER STREAM")
            (do-every 60500 (partial handle-mentions-stream creds user-name mentions-stream)))))
