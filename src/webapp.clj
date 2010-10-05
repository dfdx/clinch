
(ns webapp
  (:gen-class)
  (:use compojure.core ring.adapter.jetty clojure.contrib.json
	semantic.linkedin)
  (:require [compojure.route :as route]))

(defroutes webservice
  (GET "/" [] "<h1>Hello world</h1>")
  (POST "/init" [] (do (semantic.linkedin/init) "Ok."))
  (POST "/classify" [json rate crate]
	(println "Request with params: rate = " rate "; context rate = " crate)
	(json-str (classify-profiles
		   json (Double/parseDouble rate) (Double/parseDouble crate))))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  ;;(println args)
  (run-jetty webservice {:port (Integer. (first args))}))
  
;;(run-jetty webservice {:port 8080})