(ns chatter.core-test
  (:require [clojure.test :refer :all]
            [chatter.core :as core]))

(deftest own-tweets-irrelevant-test
  (testing "Ensure the bot's tweets are irrelevant"
    (is (#'core/irrelevant-mention? ::name {:user {:screen_name ::name}
                                       :text "stuff"}))))

(deftest retweets-irrelevant-test
  (testing "Ensure retweets of the bot's tweets are irrelevant"
    (is (#'core/irrelevant-mention? ::name {:user {:screen_name ::not_name}
                                       :retweeted_status {:user {:screen_name ::name} :text "@somebody blah"}
                                       :text "@someoneelse blah"}))))
