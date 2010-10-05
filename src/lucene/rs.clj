
(ns lucene.rs
  (:use clojure.contrib.pprint clojure.contrib.repl-utils
	clojure.contrib.math clojure.contrib.str-utils)
  (:import asi.simplelucene.Doc
	   asi.simplelucene.Index
	   (org.apache.lucene.index Term TermEnum IndexReader)
	   java.util.UUID
	   java.io.File
	   org.apache.lucene.analysis.snowball.SnowballAnalyzer))


(def CONTENTS "contents")

;; (def PATH "/media/sda5/indices")

(defn make-index [path] (Index. path))

;; (def index (make-index PATH))

(def standard-analyzer (SnowballAnalyzer. "English"))

(defn analyze
  "Breaks text into list of terms using specified anayzer"
  ([text] (analyze text standard-analyzer))
  ([text analyzer]
     (let [reader (java.io.StringReader. text)
	   tokens (.tokenStream analyzer nil reader)
	   ret (ref [])
	   token (ref (.next tokens))]
       (while (not (nil? @token))
	 (dosync
	  (alter ret conj (.term @token))
	  (ref-set token (.next tokens))))
       @ret)))


(defn all-words [index]
  (seq (.allWords index)))

(defn all-doc-nums [index]
  (seq (.allDocNums index)))

(defn all-doc-ids [index]
  (seq (.allDocIds index)))

(defn- dict-to-doc [dict]
  (reduce #(do (.put %1 (first %2) (second %2)) %1)
	  (Doc.)
	  dict))

(defn- doc-to-dict [doc]
  (reduce #(conj %1 %2) {} doc))

(defn search [index query]
  (map doc-to-dict (.search index query)))

(defn search-and-show [index query]
  (let [results (search index query)]
    (dorun 
     (map #(do (println "ID       : " (% "id")) 
	       (println "TITLE    : " (% "title"))
	       (println "CONTENTS : " (apply str (take 100 (% "contents"))))
	       (println)) 
	  results))))

(defn add-document [index dict]
  (.addDoc index (dict-to-doc dict)))

(defn delete-document [index doc-id]
  (.deleteDoc index doc-id))

(defn update-document [index id-doc new-doc]
  (.updateDoc index id-doc (dict-to-doc new-doc)))

(defn detect-lang [text]
  (Index/detectLang text))

(defn retrieve [index doc-id]
  (doc-to-dict (.retrieve index doc-id)))

;; (defn term-docs [index field val]
;;   (.termDocs index field val))

;; (defn make-frequency-function [index field]
;;   (fn [doc-num val]
;;     (.frequency index doc-num field (str-join " " (analyze val)))))

(defn freq-in-docs [index word]
  (map (fn [[doc, freq]] [doc, freq]) (.freqInDocs index word)))