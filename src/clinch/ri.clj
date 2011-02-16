
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
   their vectors, if any. Analyzer specifies language. If no analyzer is
   supplied, language detection is used. Takes O(W) time, where W -
   number of words in text. "
  ([vbox text] (context-vector vbox text (clucy/auto-analyzer)))
  ([vbox text analyzer]
     (let [words (clucy/analyze text analyzer)]
       (reduce vec/plus (map #(wcontext-vector vbox %) words)))))



;; (defn collect-parents [buck depth]
;;   (cond
;;    (empty? (buck 'parents)) #{}
;;    (> depth 0) 
;;    (into (buck 'parents)
;; 	 (reduce-into (map #(collect-parents % (- depth 1)) (buck 'parents))))
;;    true #{}))


(defn collect-parents [buck depth]
  (cond
   (empty? @(:parents buck)) #{}
   (> depth 0) 
   (into @(:parents buck)
	 (reduce rinto (map #(collect-parents % (- depth 1))
			    @(:parents buck))))
   true #{}))



(defn sim
  ([vbox text1 text2] (sim vbox text1 text2 clucy/*analyzer*))
  ([vbox text1 text2 analyzer]
     (let [v1 (context-vector vbox text1 analyzer)
	   v2 (context-vector vbox text2 analyzer)]
       (vec/cosine v1 v2))))
