
(ns semantic.db.skills
  (:use clojure.contrib.sql clojure.contrib.repl-utils))

(let [db-host "localhost"
      db-port 3306
      db-name "questions_dev"]
  (def db {:classname "com.mysql.jdbc.Driver"
	   :subprotocol "mysql"
	   :subname (str "//" db-host ":" db-port "/" db-name)
	   :user "mixtent"
	   :password "sk1llskud0s"}))

(defn create-blogs []
  (create-table
   :blogz
   [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
   [:title "varchar(255)"]
   [:body :text]))



(defn retrieve-skills []
  (with-connection db
    (with-query-results rs
      ["select skill_id, skill_name, parents, keywords from skill"]
      ;;(doall (reduce conj
      ;;	     (map (fn [skill] { (:skill_id skill) skill } ) rs))))))
      (sort-by #(:skill_id %) rs))))