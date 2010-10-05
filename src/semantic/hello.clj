
(ns semantic.hello
  (:gen-class
   :methods [[sayhello [] void]]))

(defn -sayhello [this] (println "Hello from semantics!"))

(defn -main [] (-sayhello nil))
