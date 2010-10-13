
(ns clinch.classification
  (:use [clinch buckets ri utils]
	[clucy.core :as clucy]))

(defn )



(defn classify-vector
  ([root v] (classify-vector root v 0))
  ([root v min-rate]
     (let [;;length (vector-length (root 'own-vector))
	   rate (cosine v (root 'own-vector))]
       (if (> rate min-rate)
	 (cons [root rate]
	       (apply concat (filter not-empty
				     (map #(classify-vector % v)
					  (root 'children)))))))))

(defn classify
  ([root vm text] (classify root vm text 0))
  ([root vm text min-rate]
     (let [v (context-vector vm text)]  ;; preprocessing must occur before
       (distinct (classify-vector root v min-rate)))))

