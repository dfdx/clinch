
(ns semantic.ri
  (:import (java.util Random) 
	   (org.apache.lucene.index Term TermEnum IndexReader))
  (:use clojure.contrib.repl-utils clojure.contrib.str-utils
	clojure.contrib.math clojure.contrib.def 
	lucene.rs semantic.vectors utils.utils))


(def VECTOR_DIM 20000)
(def VECTOR_SEED 1)


(defn vector-map [index]
  (let [docs (ref {})
	words (ref {})]
    (letfn
	[(get-vector [num] (@docs num))
	 (get-word-vector [w] (@words w))
	 (inner-word-context-vector
	  [word] (let [doc-freq-pairs (freq-in-docs index word)]
	       (reduce add (map #(mult-const (get-vector (first %)) 
					     (second %)) 
				doc-freq-pairs))))
	 (update-words
	  [] (dosync
	      (ref-set words {})
	      (doseq [word (all-words index)]
		(alter words conj
		       { word (inner-word-context-vector word) }))))
	 (add-new-docs
	  [] (dosync
	      (doseq [num (all-doc-nums index)]
		(when (nil? (docs num))
		  (alter
		   docs conj
		   { num (make-random-vector VECTOR_DIM VECTOR_SEED) })
		  (update-words)))))
	 (dispatch
	  [msg & args]
	  (cond (= msg 'docs) @docs
		(= msg 'words) @words
		(= msg 'update-words) (update-words)
		(= msg 'add-new-docs) (add-new-docs)
		(= msg 'get-vector) (get-vector (first args))
		(= msg 'get-word-vector) (get-word-vector (first args))
		true (throw (Exception. "vector-map: Unknown message"))))]
      dispatch)))

(defn word-context-vector
  "Don't forget to put here only analyzed words!"
  [vm word]
  (vm 'get-word-vector word))

;; may generate an error
(defn context-vector [vm text]
  (reduce add (map #(word-context-vector vm %) (analyze text))))
    
(defn vector-closeness [v1 v2]
  (cosine v1 v2))

(defn sim [vm t1 t2]
  (vector-closeness (context-vector vm t1) (context-vector vm t2)))


;; (defn word-context-vector [vm word]  
;;   (let [doc-freq-pairs (freq-in-docs word)]
;;     (reduce add (map #(mult-const (vm 'get-vector (first %)) 
;; 				  (second %)) 
;; 		     doc-freq-pairs))))


;; (defn make-initial-matrix [index]
;;   (reduce conj 
;; 	  (map #(sorted-map % (make-random-vector +vector-dim+ +vector-seed+)) 
;; 	       (all-doc-nums index))))

;; (declare matrix)

;; (defn save-matrix [f matrix]
;;   (spit f matrix))

;; (defn load-matrix [f]
;;   (read-string (slurp f)))

;; (defn get-vector [matrix id]
;;   (matrix id))

;; context vectors ;;
;; ???
;; (defn split-text [text]
;;   (filter #(not (or (empty? %)))
;; 	  (re-split #"[.,:;=\-?!&<>{} \n]+" (.toLowerCase text))))

;; (defn make-similarity-function [index matrix field]
;;   (fn [w1 w2] 
;;     (vector-closeness (context-vector index matrix w1)
;; 		      (context-vector index matrix w2))))


;; (def sim (make-similarity-function index matrix +contents+))

;; (declare words)

;(def terms (lucene-terms index))

;; (defn make-stack [] '())

;; (defnk insert [comp val stack :size 10] 
;;   (cond (empty? stack) (cons val stack)
;; 	(comp val (first stack)) (if (>= (count stack) size) ; if stack's full
;; 				   (insert comp val (rest stack))
;; 				   (cons (first stack) 
;; 					 (insert comp val (rest stack))))
;; 	(< (count stack) size) (cons val stack)
;; 	true stack))


;; (defn take-by [comp n seq]
;;   (let [res (insert 'foo (first seq) (make-stack))]
;;     (reduce #(insert comp %2 %1 :size n) res (rest seq))))

;; (defn find-similar [word n]
;;   (let [v (context-vector index matrix word)]
;;     (take n 
;; 	  (sort #(>= (second %1) (second %2)) 
;; 		(distinct 
;; 		 (map 
;; 		  (fn [w]
;; 		    [w
;; 		     (vector-closeness v (context-vector index matrix w))])
;; 		  words))))))


;; (defn find-similar [word n]
;;   (let [v (context-vector *index* *matrix* (Term. +contents+ word))]
;;     (take-by 
;;      #(>= (second %1) (second %2))
;;      n 
;;      (distinct ; why there're duplicates??
;;       (map (fn [t] [(.text t)
;; 		    (vector-closeness v (context-vector *index* *matrix* t))])
;; 	   terms)))))

;; (defn initialize []
;;   (add-document index {+contents+ "bar"})
;;   (def matrix (make-initial-matrix index))
;;   (def words (all-words index)))