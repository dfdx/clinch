
(ns semantic.linkedin
  (:use [clojure.contrib json str-utils def pprint]
	clojure.java.io
   ;;clojure.contrib.json clojure.contrib.str-utils clojure.contrib.def
   ;;clojure.contrib.pprint
   
	utils.utils semantic.vectors semantic.db.skills
	semantic.classifier semantic.ri lucene.rs)
  (:import java.util.HashMap java.util.ArrayList)
  (:gen-class
   :methods [[init [void] void]
	     [classify [String] java.util.Map]]))

(def json (slurp "/media/sda5/data_Jon.txt"))
(def people (map second (read-json json nil)))

(defn get-position-info [person]
  (if-not (empty? (person "positions"))
    (letfn [(get-pos-info
	     [pos]
	     (str-join " -- "
		       [(if (empty? (pos "title")) "" (pos "title"))
			(if (empty? (pos "summary")) "" (pos "summary"))]))]
      (let [positions ((person "positions") "position")]
	(cond (empty? positions) ""
	      (is-map? positions) (get-pos-info positions)
	      (is-vector? positions) (str-join
				      " -- " (map get-pos-info positions))
	      true (throw
		    (Exception.
		     (str "Don't know how to treat this type of positions: "
			  (type positions)))))))
    ""))

(defn get-education-info [person]
  (if-not (empty? (person "educations"))
    (letfn [(get-edu-info
	     [edu] (str-join
		    " -- "
		    [(if (empty? (edu "school-name")) "" (edu "school-name"))
		     (if (empty? (edu "notes")) "" (edu "notes"))
		     (if (empty? (edu "field-of-study")) ""
			 (edu "field-of-study"))]))]
      (let [educations ((person "educations") "education")]
	(cond (empty? educations) ""
	      (is-map? educations) (get-edu-info educations)
	      (is-vector? educations) (str-join
				       " -- " (map get-edu-info educations))
	      true (throw
		    (Exception.
		     (str "Don't know how to treat this type of positions: "
			  (type educations)))))))
    ""))

(defn get-person-info [person]
  (str-join " " [(person "headline") (person "industry")
		 (get-position-info person) (get-education-info person)]))

(defn get-info [#^String linkedin-json-str]
  (let [people (map second (read-json linkedin-json-str nil))
	ret (ref {})]
    (dosync
     (doseq [person people]
       (alter ret conj { (str (person "first-name") " " (person "last-name")
			      " (" (person "id") ")")
			 (str-join
			  " " [(person "headline") (person "industry")
			       (get-position-info person)
			       (get-education-info person)]) }))) 
    @ret))

(defnk find-person [people :fname nil :lname nil]
  (filter #(if (not (nil? fname))
	     (= (.toLowerCase (% "first-name")) fname) true)
	  (filter #(if (not (nil? lname))
		     (= (.toLowerCase (% "last-name")) lname) true)
		  people)))

(defn pinfo [people name]
  (let [[fname lname] (re-split #"[ ]" name)
	found (find-person people :fname fname :lname lname)]
    (get-person-info (first found))))

(defn preprocess [#^String text]
  (replace-all
   (.. text toLowerCase
       (replace "c++" "cplusplus")
       (replace ".net" "dotnet"))
   { "c#" "csharp"
     "software engineer" "softwareengineer"
     "software develop" "softwaredevelop"
     "software architect" "softwarearchitect"
     "front-end" "frontend"
     "front end" "frontend"
     "back-end" "backend"
     "back end" "backend"
     "objective-c" "objectivec"
     "ruby on rails" "rubyonrails"
     "c shell" "cshell"
     "visual basic" "visualbasic"
     "facebook api" "facebookapi"
     "linkedin api" "linkedinapi"
     "artificial intelligence" "artificialintelligence"
     "computer science" "computerscience"
     "machine learning" "machinelearning"
     "data mining" "datamining"
     "computer vision" "computervision"
     "cloud computing" "cloudcomputing"
     "map/reduce" "mapreduce"
     "system administr" "systemadminist"
     "data architect" "dataarchitect"
     "web design" "webdesign"
     "graphic design" "graphicdesign"
     "product design" "productdesign"
     "user interface" "userinterface"
     "userexperience" "userexperience"
     "human computer interaction" "humancomputerinteraction"
     "project manage" "projectmanage"
     "product manage" "productmanage"
     "community manage" "communitymanage"
     "social media" "socialmedia"
     "online marketing" "onlinemarketing"
     "online advertis" "onlineadvertis"
     "email marketing" "emailmarketing"
     "ad real sate" "adrealsate"
     "financial advise" "financialadvise"
     "finansial analisys" "finansialanalys"
     "venture capital" "venturecapital"
     "angel invest" "angelinvest"
     "chief financial officer" "chieffinancialofficer"
     "chief executive officer" "chiefexecutiveofficer"
     "search engine optimization" "searchengineoptimization"
     "search engine marketing" "searchenginemarketing"
     "recommendation engine" "recommendationengine"
     "search algorithm" "searchalgorithm"
     "public policy" "public policy"
     "political science" "politicalscience"
     "corporate law" "corporatelaw"
     "divorce lawyer" "divorcelawyer"
     "startup lawyer" "startuplawyer"
     "renewable energy" "renewableenergy"}))

(defn indexed-bucket [index vm name text]
  (let [new-text (preprocess text)]
    (add-document index { CONTENTS new-text })
    (vm 'add-new-docs)
    (bucket vm name new-text)))

(defn classify-with-filters
  "Applies all filters to classification result"
  ([root vm text] (classify-with-filters root vm text 0.0 0.03))
  ([root vm text min-rate] (classify-with-filters root vm text min-rate 0.03))
  ([root vm text min-rate c-min-rate]
     ;;(println (time (classify root vm (preprocess text) min-rate)))
     ;;(println "\n\n\n\n\n\n\n======================\n\n\n\n\n\n\n")
     (filter-by-parents
      (classify root vm (preprocess text) min-rate) c-min-rate)))

(defn format-results [results]
  (reduce-into (map (fn [[buck rate]] { (buck 'name) rate }) results)))

(defn classify-linkedin-json [root vm #^String json min-rate c-min-rate]
  (let [all-info (get-info json)]
    (reduce-into
     (map (fn [[name info]]
	    { name (format-results
		    (classify-with-filters
		      root vm info min-rate c-min-rate)) })
	  all-info))))

(defn avoid-map-bug [m]
  (apply sorted-map (flatten (map identity m))))

(defn skills-by-name [skills]
  (reduce conj (map (fn [skill] { (:skill_name skill) skill } ) skills)))


(defn make-root-bucket-from-db [index vm skills]
  (let [buckets (ref (sorted-map))
	by-name (skills-by-name skills)
	root (bucket vm "Root" "foobarbaz")]
    (dosync
     (doseq [sk skills]
       (let [buck (indexed-bucket
		   index vm (:skill_name sk)
		   (.replace
		    (.replace (str (:skill_name sk) ";" (:keywords sk))
			      " " "") "-" ""))]
	 (alter buckets conj { (:skill_id sk) buck } ))))
    ;; installing children
    (doseq [b (map #(second %) @buckets)]
      (let [sk (by-name (b 'name))]
	(if (= (:parents sk) "")
	  (root 'install root b)
	  (doseq [parent-num
		  (map #(Integer/parseInt %)
		       (re-split #";" (.replace (:parents sk) " " "")))]
	    (let [parent (buckets parent-num)]
	      (parent 'install parent b))
	    ))))
    root))

(defn init-from-db [settings]
  (let [index (make-index (settings :index-path))]
    (def vm (vector-map index))
    ;;(def root (make-root-bucket index vm))
    (def root (make-root-bucket-from-db
	       index vm (semantic.db.skills/retrieve-skills)))))


(defn cleanup-index [settings]
  (doseq [file (list-dir (:index-path settings))]
    (delete-file file)))

(let [initialized (ref false)]
  (defn init []
    (let [settings {:index-path "/tmp/indices/"}]
      (cleanup-index settings)
      (init-from-db settings)
      (dosync (ref-set initialized true))))
  
  (defn classify-profiles [json min-rate c-min-rate]
    (when (not @initialized)
      (init))
    (classify-linkedin-json root vm json min-rate c-min-rate)))


(defn show-tree [root spaces]
  (doseq [i (range spaces)] (print " "))
  (println (root 'name))
  (doseq [child (root 'children)] (show-tree child (+ spaces 2))))

;; tests

(defn test-it []
  (binding [*out* (java.io.PrintWriter. "/media/sda5/output.txt")]
    (let [json (slurp "/media/sda5/data_Jon.txt")]
      (doseq [[name bucks] (time (classify-profiles json 0 0.03))] 
 	(cl-format *out* "~20a~a" name " :: ")
 	(doseq [[b-name rate] bucks]
 	  (when (not= b-name "Root")
 	    (print (str b-name "; " ))))
 	(println)))))


(def ii "The Business of Fashion Apparel & Fashion Managing Director -- Amed & Company is a boutique advisory firm, working with global fashion businesses and high-potential fashion start-ups. The firm leverages a network of strategic partners to deliver strategic advice, M&A advisory, and branding strategy. -- Associate Lecturer --  -- Founder and Editor --  -- Co-Founder and Editor-in-Chief --  -- Co-Founder -- Investing in and building businesses for emerging fashion design talent in London and elsewhere. -- Engagement Manager --  -- Associate --  Harvard Business School --  --  -- McGill University --  --  -- The Conservatory of Performing Arts - Mount Royal College --  -- Drama, Speech Arts and Public Speaking java")

(defn do-1 [name]
  (def i (pinfo people name))
  (println i))

(defn do-2 [k1 k2]
  (map (fn [[buck rate]] (str (buck 'name) "/"
			      (apply str (take 5 (str rate))))) 
       (classify-with-filters root vm i k1 k2)))


;; daniel cohen, sandra levy