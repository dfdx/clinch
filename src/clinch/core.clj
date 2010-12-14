
(ns clinch.core
  (:use [clojure.contrib def])
  (:require [clucy.core :as clucy]
	    [clinch.ri :as ri]
	    [clinch.buckets :as buckets]
	    [clinch.classification :as clf]))

(defn indexed-bucket
  "Creates Lucene document from text and then makes vector-box
   on top of this box. Side effects: updated index and vbox

   NOTE: Multi-word terms must be replaced with one-word strings! Ex.:
      Web development with Adobe Flash => Webdevelopment with AdobeFlash. "
  ([index vbox name text] (indexed-bucket
			   index vbox (clucy/auto-analyzer text) name text))
  ([index vbox analyzer name text]
     (let [new-text text]
       (clucy/add index analyzer { :contents new-text })
       (ri/update! vbox)
       (buckets/make-bucket vbox name new-text :analyzer analyzer))))


(defnk make-index [:path nil]
  (if (empty? path)
    (clucy/memory-index)
    (clucy/disk-index path)))

;; (defn add-document [index & maps]
;;   (apply #(clucy/add index %) maps))

(defn make-vector-box [idx]
  (ri/make-vector-box idx))

(defn make-bucket-row [idx vbox bucket-map analyzer]
  (map #(indexed-bucket idx vbox analyzer (first %) (second %)) bucket-map))

(defn classify
  ([root vbox text] (clf/classify root vbox text))
  ([root vbox text thsd] (clf/classify root vbox text thsd)))

(defn find-best-in-row [row vbox text]
  (clf/find-best-in-row row vbox text))



(defn auto-analyzer [text]
  (clucy/auto-analyzer text))