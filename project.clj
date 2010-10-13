
(defproject clinch "1.0.0-SNAPSHOT"
  :description "Semantic classifier based on Random Indexing"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [org.apache.lucene/lucene-core "3.0.1"]
		 [org.apache.lucene/lucene-snowball "3.0.1"]
		 [org.apache.tika/tika-core "0.7"]
		 ;; clucy
		 ]
  :dev-dependencies [[swank-clojure "1.2.1"]])
