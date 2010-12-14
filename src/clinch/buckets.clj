
(ns clinch.buckets
  (:require [clucy.core :as clucy])
  (:use [clojure.contrib def]
	[clinch ri utils]
	[clinch.vectors :as vec]))

(defstruct
    #^{:doc "Bucket to classify text to. "}
  bucket :name :text :store-text :own-vector :parents :children :analyzer)

(defnk make-bucket
  "Creates classification bucket. If :store-text is true, will also store
   indexed text. You can create bucket hierarchies by using (add-child)
   procedure. "
  [vbox name text :store-text true :analyzer clucy/*analyzer*]
  (with-meta
    (struct-map bucket
      :name name
      :text (if store-text text nil)
      :store-text store-text
      :own-vector (atom (context-vector vbox text analyzer))
      :parents (atom #{})
      :children (atom #{})
      :analyzer analyzer)
    {:type ::bucket}))
  
;; wrappers to not mess with atoms by hand

(defn own-vector [buck]
  @(:own-vector buck))

(defn bucket-name [buck]
  (:name buck))

(defn bucket-text [buck]
  (:text buck))

(defn- add-vector! [buck v]
  (swap! (:own-vector buck) vec/plus v)
  (doseq [parent @(:parents buck)]
    (add-vector! parent v)))

(defn- sub-vector! [buck v]
  (swap! (:own-vector buck) vec/minus v)
  (doseq [parent @(:parents buck)]
    (sub-vector! parent v)))

(defn add-child! [parent child]
  (swap! (:parents child) conj parent)
  (swap! (:children parent) conj child)
  (add-vector! parent @(:own-vector child)))

(defn remove-child! [parent child]
  (swap! (:parents child) disj parent)
  (swap! (:children parent) disj child)
  (sub-vector! parent @(:own-vector child)))

(defn- drop-all-except-name [buck]
  {:name (:name buck) :others :...})

(defmethod print-method ::bucket
  [buck writer]
  (let [new-parents
	(atom (reduce conj #{} (map drop-all-except-name @(:parents buck))))
	new-children
	(atom (reduce conj #{} (map drop-all-except-name @(:children buck))))]
    (print-method
     {:name (:name buck)
      :text (apply str (take 100 (:text buck)))
      :store-text (:store-text buck)
      :own-vector (:own-vector buck)
      :parents new-parents
      :children new-children }
     writer)))


;(assoc buck :children new-children)

;; util functions

(defn find-bucket [root name] 
  (cond (= name (:name root)) root
	(empty? @(:children root)) nil
	:else (first
	       (filter #(not= % nil)
		       (map #(find-bucket % name) @(:children root))))))