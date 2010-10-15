
(ns clinch.ri
  (:use [clojure.contrib def]
	[clinch utils])
  (:require [clucy.core :as clucy]
	    [clinch.vectors :as vec]))


(defstruct 
    #^{:doc "Struct to keep vectors for docs and words. "}
  vector-box :index :doc-init-vectors :word-vectors :dim :seed)

(defnk make-vector-box
  "Creates vector box around specified index. Additionally can accept vectors'
   dimensions count (:dim, defaults to 5000) and number of non-zero elements
   (:seed, defaults to 10). For example, to create vector box with 10000
   dimentions and 5 non-zero elements, use:

   (make-vector-box idx :dim 10000 :seed 5)"
  [index :dim 5000 :seed 10]
  (with-meta
    (struct-map vector-box
      :index index
      :doc-init-vectors (atom {})
      :word-vectors (atom {})
      :dim dim 
      :seed seed)
    {:type ::vector-box}))

;; (defmethod clojure.core/print-method ::vector-box [vbox w]
;; 	   (println "box!"))

(defn word-vector [vbox word]
  (@(:word-vectors vbox) word))

(defn doc-init-vector [vbox n]
  (@(:doc-init-vectors vbox) n))


(defn- add-vec! [vbox w v]
  (swap! (:word-vectors vbox) assoc w (vec/plus (word-vector vbox w) v))
  nil)

(defn- sub-vec! [vbox w v]
  (swap! (:word-vectors vbox) assoc w (vec/minus (word-vector vbox w) v))
  nil)


(defn- find-new-docs [vbox]
  (let [all-docs (clucy/all-doc-numbers (:index vbox))
	current-docs (keys @(:doc-init-vectors vbox))]
    (seq (apply disj (set all-docs) current-docs))))

(defn- find-new-words [vbox]
  (let [all-words (clucy/all-words (:index vbox))
	current-words (keys @(:word-vectors vbox))]
    (seq (apply disj (set all-words) current-words))))

;;(defn find-words-to-update [vbox]
;;  (flatten (map (analyze ) )))


(declare
 calculate-context-vector
 wcontext-vector
 context-vector)


(defn- update-docs! [vbox]
  (let [new-docs (find-new-docs vbox)
	new-vectors (reduce
		     rconj (map (fn [n] {n (vec/make-random-vector
					   (:dim vbox) (:seed vbox))})
			       new-docs))]
    (swap! (:doc-init-vectors vbox) conj new-vectors)
    new-docs))

;; (defn- update-words! [vbox]
;;   (let [new-vectors (reduce
;; 		     rconj (map (fn [w] {w (calculate-context-vector vbox w)})
;; 				(find-new-words vbox)))]
;;     (swap! (:word-vectors vbox) conj new-vectors)))

(defn- update-words! [vbox new-docs]
  (doseq [d new-docs]
    (let [d-vec (doc-init-vector vbox d)]
      (doseq [w (clucy/document-words (:index vbox) d)]
	(add-vec! vbox w d-vec))))
  nil)

(defn update! [vbox]
  (update-words! vbox (update-docs! vbox)))

(defn reset-vectors! [vbox]
  (let [doc-map (reduce
		 rconj (map (fn [n] {n (vec/make-random-vector
					(:dim vbox) (:seed vbox))})
			    (clucy/all-doc-numbers (:index vbox))))]
    (reset! (:doc-init-vectors vbox) doc-map)
    (let [word-map (reduce
		    rconj (map (fn [w] {w (calculate-context-vector vbox w)})
			       (clucy/all-words (:index vbox))))]
      (reset! (:word-vectors vbox) word-map))))


(defn calculate-context-vector
  "Calculates context vector for a given word by summation of vectors of
   documents the word occured in. Takes O(n) time. Low-level procedure,
   for most cases you must use (context-vector) instead. "
  [vbox word]
  (let [doc-to-count-list (clucy/count-per-doc (:index vbox) word)]
    (reduce vec/plus (map #(vec/mult-const (doc-init-vector vbox (first %)) 
				       (second %)) 
			  doc-to-count-list))))

(defn wcontext-vector
  "Retrieves word's context vector from vbox. Takes O(1) time. Low-level
   procedure, for most cases you must use (context-vector) instead. "
  [vbox word]
  (word-vector vbox word))

(defn context-vector
  "Produces text's context vector by traversing all words and summation
   their vectors, if any. Takes O(W), where W - number of words in text. "
  [vbox text]
  (let [words (clucy/analyze text)]
    (reduce vec/plus (map #(wcontext-vector vbox %) words))))



(defn init-test []
  (def idx (clucy/disk-index "/home/asi/index"))
  (def vbox (make-vector-box idx :dim 5000 :seed 5))
  (update! vbox))