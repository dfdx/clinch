
(defproject clinch/clinch "0.5.0"
  :description "Semantic classifier based on Random Indexing"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [org.apache.lucene/lucene-core "3.0.3"]
		 [org.apache.lucene/lucene-snowball "3.0.3"]
		 [org.apache.tika/tika-core "0.7"]
		 [clinch/clucy "0.1.3"]]
  :dev-dependencies [[swank-clojure "1.2.1"]])
