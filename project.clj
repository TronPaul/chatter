(defproject chatter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [markov-text "0.0.2"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main ^:skip-aot chatter.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
