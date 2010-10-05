
(defproject semantic "1.0.0-SNAPSHOT"
  :description "general project to use for semantic task"
  :dependencies [[org.clojure/clojure "1.2.0-beta1"]
                 [org.clojure/clojure-contrib "1.2.0-beta1"]
		 [compojure "0.4.0"]
		 [ring/ring "0.2.3"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :namespaces [semantic.classifier semantic.linkedin webapp]
  :main webapp)