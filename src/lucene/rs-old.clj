
(ns ilib.rs-old
  (:use clojure.contrib.pprint clojure.contrib.repl-utils
	clojure.contrib.math)
  (:import (org.bsuir.commons.rs.lucene RSImpl DocImpl FieldImpl)
	   (org.apache.lucene.index Term TermEnum IndexReader)
	   (java.util UUID)
	   java.io.File))

;(import '(org.bsuir.commons.rs.lucene RSImpl))

(def rs (RSImpl/getInstance))

(def path "/media/Media/Misc/Tests/lucene-index")
(def user-path "/media/Media/Misc/Tests/lucene-user-index")

;(def fields (make-array String 2))
;(aset fields 0 "contents")
;(aset fields 1 "title")


;(defn make-search-string [pairs]
;  (apply str (map #(str (first %) ":(" (second %) ") ") pairs)))

;; usage:
;; (search path "title:macbeth creator:\"william shakespeare\" contents:king")
;; (search path '(["title" "macbeth"] 
;;                ["creator" "william shakespeare"] 
;;                ["contents" "king"]))

;; (defn title-snippet [res]
;;   (let [snip (.get (.getSnippets res) "title")]
;;     (if (not= snip "")
;;       snip
;;       (.getField (.getDoc res) "title"))))

;; (defn contents-snippet [res]
;;   (let [snip (.get (.getSnippets res) "contents")]
;;     (if (not= snip "")
;;       snip
;;       "<i>no snippet</i>")))

;; (defn print-results [results] 
;;   (let [formatted (map #(cl-format true "----------~%TITLE: ~a~%CONTENTS: ~a~%"
;; 				   (title-snippet %)
;; 				   (contents-snippet %)) 
;; 		       results)]
;;     (println (apply str formatted))))


;(defn search 
;  ([path query] (search path query print-results))
;  ([path query printer] 
;     (let [results 
;	   (seq (.searchWithSnippet rs path query "EN" fields))]
;       (printer results))))

(defn search [path query]
  (let [results (seq (.search rs path query "EN"))]
    results))

(defn hightlight [text query]
  (.hightlight rs text query "EN"))

(defn contents [results]
  (for [res results] 
    (println (str 
	      (apply str (take 100 (.getField (.getDoc res) "contents")))
	      "....\n==="))))



(defn make-rs-doc [& doc-fields]
  (let [rs-doc (DocImpl.)]
    (doall (map #(.addField rs-doc 
			    (FieldImpl. (first %) (second %) true true)) 
		doc-fields))
    rs-doc))

(defn add-doc 
  "usage: (add-doc path ['title' 'the tragedy of Macbeth']
                        ['contents' 'blah blah blah'])"
  [path & doc-fields]
  (let [rs-doc (apply make-rs-doc doc-fields)]
    (.addDoc rs path rs-doc "EN")))

(defn delete-doc [index-w doc]
  (throw (Exception. "Not implemented")))
	
	
;; (defn add-user [username pass roles groups]
;;   (let [roles-str (reduce #(str %1 " " %2) roles)
;; 	groups-str (reduce #(str %1 %2) groups)]
;;     (add-doc user-path 
;; 	     ["userId" (str (UUID/randomUUID))]
;; 	     ["type" "user"]
;; 	     ["username" username]
;; 	     ["password" pass]
;; 	     ["roles" roles-str]
;; 	     ["groups" groups-str])))


;; terms utils

;; lucene 

(defn is-word [text]
  (every? #(Character/isLetter %) text))

;; (defn is-word [term]
;;   (let [length (.length term)]
;;     (loop [i 0]
;;       (cond (= i length) true
;; 	    (not (Character/isLetter (.charAt term i))) false
;; 	    true (recur (inc i))))))


(def stop-words #{})
(def stop-fields #{ "path" })

(defn standard-term-filter [term] 
  (let [term-text (.text term), term-field (.field term)]
    (and (not (contains? stop-words term-text))
	 (not (contains? stop-fields term-field))
	 (is-word term-text))))     ;; it makes retirving terms 5(!!!) times longer
	 

;; (defn lucene-terms 
;;   "Gets all the terms from specified index. 
;;    Index must be already opened."
;;   ([index] (lucene-terms index standard-term-filter))
;;   ([index term-filter]
;;      (let [terms-enum (.terms index)]
;;        (loop [terms '()]
;; 	 (if (.next terms-enum)
;; 	   (if (term-filter (.term terms-enum))
;; 	     (recur (cons (.term terms-enum) terms))
;; 	     ; (recur (cons (.text (.term terms-enum)) terms))
;; 	     (recur terms))	  
;; 	   (reverse terms))))))

(defn lucene-terms 
  "Gets all the terms from specified index. 
   Index must be already opened."
  ([index] (lucene-terms index standard-term-filter))
  ([index term-filter]
     (let [terms-enum (.terms index)]
       (let [terms (atom '())]
	 (while (.next terms-enum)
	   (if (term-filter (.term terms-enum))
	     (swap! terms #(cons (.term terms-enum) %))))	  
	 (reverse @terms)))))

(defn lazy-lucene-terms 
  ([index] (lazy-lucene-terms index standard-term-filter))
  ([index terms-filter]
     (let [terms-enum (.terms index)
	   get-next-term (fn next-one [] 
			   (if (.next terms-enum)
			     (cons (.term terms-enum)
				   (lazy-seq (next-one)))
			     '()))]
       (filter terms-filter (get-next-term)))))

(defn lucene-words [index field]
  (map #(.text %) (filter #(= (.field %) field) (lucene-terms index))))

(defn lucene-docs [index]
  (let [num (.numDocs index)]
    (filter #(not (.isDeleted index %)) (range num))))

(defn make-freq-func 
  "Creates closure around specified field returning 
  list of [doc, freq] pairs for given index and word" 
  [field]
  (fn [index word]
    (let [term-docs (.termDocs index (Term. field word))
	  res (atom '())]
      (while (.next term-docs)
	(swap! res 
	       #(cons [(.doc term-docs) (.freq term-docs)] %)))
      (reverse @res))))

(def freq (make-freq-func "contents"))


;; (defn lazy-test []
;;   (let [numbers (fn numb [n]
;; 		  (if (< n 10)
;; 		    (cons n (lazy-seq (numb (+ n 1))))
;; 		    '()))]
;;     (numbers 0)))


;;;;; utils ;;;;;

(def text-books "/media/Media/Misc/Tests/textbooks/")

(defn add-text-file [filename]
  (add-doc path ["contents" (slurp filename)]))

(defn list-dir [dir-path]
  (map #(.getPath %) (.listFiles (File. dir-path))))

(defn add-dir [dir-path]
  (let [filenames (list-dir dir-path)]
    (map add-text-file filenames)))

(defn search-contents [word]
  (contents (search path (str "contents:(" word ")"))))