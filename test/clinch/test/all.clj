
(ns clinch.test.all
  (:use [clinch buckets ri vectors] :reload-all)
  (:use [clojure.test])
  (:require [clucy.core :as clucy]))


(deftest update-words 
  (let [idx (clucy/memory-index)]
    (clucy/add
     idx
     {:content "one nine"}
     {:content "one two"}
     {:content "one two one"})
    (let [vbox (make-vector-box idx)]
      (update! vbox)
      (is (= (wcontext-vector vbox "one")
	     (reduce plus [(doc-init-vector vbox 0)
			   (doc-init-vector vbox 1)
			   (doc-init-vector vbox 2)
			   (doc-init-vector vbox 2)]))
	  "Error in update! function")
      (clucy/add idx {:content "just nine"})
      (update! vbox)
      (is (= (wcontext-vector vbox "nine")
	     (plus (doc-init-vector vbox 0) (doc-init-vector vbox 3)))))))
      




(deftest buckets-hierarchy
  (let [idx (clucy/memory-index)]
    (clucy/add
     idx
     {:content "This is all about art"}
     {:content "Listening music is a good relaxation method"}
     {:context "Nirvana is one of the most popular grange bands"}
     {:content "Tom Yorke, lead of Radiohed, is quite interesting person"})
    (let [vbox (make-vector-box idx)]
      (update! vbox)
      (let [art (make-bucket vbox "Art" "art")
	    music (make-bucket vbox "Music" "music")	  
	    nirvana (make-bucket vbox "Nirvana" "nirvana, kurt cobain, grange")
	    radiohead (make-bucket vbox "Radiohead" "radiohead, tom yorke")
	    start-art-vector @(:own-vector art)
	  start-music-vector @(:own-vector music)]
	(add-child! art music)
	(add-child! music nirvana)
	(add-child! music radiohead)
	(is (= @(:own-vector art)
	       (plus start-art-vector
		     (plus start-music-vector
			   (plus @(:own-vector nirvana)
				 @(:own-vector radiohead)))))
	    "Error in add-child! function. ")
	(remove-child! music nirvana)
	(is (= @(:own-vector art)
	       (plus start-art-vector
		     (plus start-music-vector
			   @(:own-vector radiohead))))
	    "Error in remove-child! function. ")))))
