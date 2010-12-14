
(ns clinch.classification
  (:use [clinch buckets ri utils])
  (:require [clinch.vectors :as vec]
	    [clucy.core :as clucy]))


(def *threshold* 0.01)


(defn get-buck [result]
  (first result))

(defn get-rate [result]
  (second result))

(defn classify-vector
  "Low-level function. Classifies vector v to all appropriate buckets from
   the tree, defined by root. Children are scaned only if correlation with
   parent is better then threshold. Returns list of pairs [bucket rate]. "
  ([root v] (classify-vector root v 0))
  ([root v threshold]
     (let [rate (vec/cosine v (own-vector root))]
       (if (> rate threshold)
	 (cons [root rate]
	       (apply concat (filter not-empty
				     (map #(classify-vector % v)
					  @(:children root)))))))))

(defn classify
  "Traverses bucket tree defined by root, collecting all appropriate buckets.
   Children are scaned only if correlation with parent is better then
   threshold. Returns list of pairs [bucket rate]. "
  ([root vbox text] (classify root vbox text *threshold*))
  ([root vbox text threshold]
     (assert (= (type root) :clinch.buckets/bucket))
     (let [v (context-vector vbox text)]  
       (distinct (classify-vector root v threshold)))))

(defn classify-flat
  "Traverses bucket row, collecting all appropriate buckets.
   Returns list of pairs [bucket rate]. "
  ([row vbox text] (classify-flat row vbox text *threshold*))
  ([row vbox text threshold]
     (assert (or (seq? row) (set? row)))
     (distinct (reduce concat (filter (complement empty?)
				      (map #(classify % vbox text) row))))))


(defn find-best [root vbox text]
  (let [results (classify root vbox text 0)]
    (reduce #(if (>= (get-rate %1) (get-rate %2)) %1 %2) results)))

;; (defn find-best-in-row [row vbox text]
;;   (let [results (classify-flat row vbox text 0)]
;;     (get-buck (reduce #(if (>= (get-rate %1) (get-rate %2)) %1 %2) results))))

(defn find-best-in-row [row vbox text]
  (let [results (classify-flat row vbox text 0)]
    (reduce #(if (>= (get-rate %1) (get-rate %2)) %1 %2) results)))


;; filters

(defn drop-by-weak-parent
  "Drops buckets that are out of context by checking their parents.
   See [...] for details. "
  [results parent-threshold parent-levels]
  (let [rate-map (apply hash-map (flatten results))
	filtered-buckets 
	(filter (fn [buck] (some #(> (rate-map %) parent-threshold)
				 (collect-parents buck parent-levels)))
		(map first results))
	needed-parents (flatten (map #(seq (collect-parents % 100))
				     filtered-buckets))
	needed-buckets (distinct (concat needed-parents filtered-buckets))]
    (map (fn [buck] [buck (rate-map buck)])
	 needed-buckets)))



;; temp

(defn init-test []
  (def index (clucy/disk-index "/home/asi/index"))
  (def vbox (make-vector-box index :dim 5000 :seed 5))
  (update! vbox)
  (def art (make-bucket vbox "Art" "art"))
  (def music (make-bucket vbox "Music" "music"))
  (def nirv (make-bucket vbox "Nirvana" "oh well, whatever, nevermind"))
  (def radio (make-bucket vbox "Radiohead" "but I'm a creep, I'm a weirdo"))
  (def apes (make-bucket vbox "Guano Apes" "Don't you turn your back on me"))
  (def eminem (make-bucket vbox "Eminem" "8 mile"))
  (def nevermind (make-bucket vbox "Nevermind" "Smells, In Bloom, Polly"))
  (def painting (make-bucket vbox "Painting" "painting"))
  (def van-gogh (make-bucket vbox "van Gogh" "sunflowers, potatos eaters,night cafe"))
  (def shiskin (make-bucket vbox "Shiskin" "Roj, mishki v lesu, korabelnaya roscha"))
  (def dali (make-bucket vbox "Dali" "masturbator, autoportait with a beacon, civic war"))
  (add-child! nirv nevermind)
  (add-child! music eminem)
  (add-child! music apes)
  (add-child! music radio)  
  (add-child! music nirv)
  (add-child! painting van-gogh)
  (add-child! painting shiskin)
  (add-child! painting dali)
  (add-child! art painting)
  (add-child! art music))


(defn show-all-docs [index]
  (doseq [d (clucy/all-doc-numbers index)]
    (println (clucy/retrieve index d))))

(defn c-show [f root vbox text]
  (doseq [[buck rate] (f root vbox text)]
    (println (:name buck) " / " rate)))


