
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


(defn make-index
  ([path] (clucy/disk-index path))
  ([] (clucy/memory-index)))

(defnk make-bucket [vbox name text :analyzer (auto-analyzer text)]
  (buckets/make-bucket vbox name text :analyzer analyzer))

;;(defn add-document [index & maps]
;;  (apply #(clucy/add index %) maps))

(defnk make-vector-box [idx :dim 5000 :seed 10]
  (ri/make-vector-box idx :dim dim :seed seed))

(defn update-vector-box! [vbox]
  (ri/update! vbox))

(defn make-bucket-row [idx vbox bucket-map analyzer]
  (map #(indexed-bucket idx vbox analyzer (first %) (second %)) bucket-map))

(defn classify
  ([root vbox text] (clf/classify root vbox text))
  ([root vbox text thsd] (clf/classify root vbox text thsd)))

(defn find-best-in-row [row vbox text]
  (clf/find-best-in-row row vbox text))


(defn auto-analyzer [text]
  (clucy/auto-analyzer text))

(defn sim
  ([vbox text1 text2] (ri/sim vbox text1 text2))
  ([vbox text1 text2 analyzer] (ri/sim vbox text1 text2 analyzer)))

