
(ns semantic.classifier
  (:use utils.utils lucene.rs semantic.vectors semantic.ri
	[clojure.contrib json str-utils def])
  (:import org.apache.commons.lang.StringUtils
	   java.util.regex.Pattern java.util.regex.Matcher)
  (:gen-class))


(defn bucket [vm name text]
  (let [own-vector (ref (context-vector vm text))
	children (ref #{})
	parents (ref #{})]
    (letfn [(add-parent [parent] (dosync (alter parents into #{ parent })))
	    (add-vector
	     [v]
	     (dosync (alter own-vector add v))
	     (doseq [parent (seq @parents)]
	       (parent 'add-vector v)))
	    (install
	     [self buck]  ;; "self" is needed since lambda doesn't have "this"
	     (dosync (alter children into #{ buck })
		     (buck 'add-parent self))
	     (add-vector (buck 'own-vector)))
	    (dispatch
	     [msg & args]
	     (cond (= msg 'name) name
		   (= msg 'text) text
		   (= msg 'own-vector) @own-vector
		   (= msg 'children) @children
		   (= msg 'parents) @parents
		   (= msg 'add-parent) (add-parent (first args))
		   (= msg 'add-vector) (add-vector (first args))
		   (= msg 'install) (install (first args) (second args))
		   true (throw (Exception. "bucket: Unknown message"))))]
      dispatch)))

(def compile-pattern 
     (memoize
      (fn [replacements]
	(let [pattern-str
	      (str "(" (StringUtils/join
			(to-array (.keySet replacements)) "|") ")")]
	  (Pattern/compile pattern-str)))))

(defn replace-all [text replacements]
  (let [pattern (compile-pattern replacements)
	matcher (.matcher pattern text)
	sb (StringBuffer.)]
    (while (.find matcher)
      (. matcher appendReplacement sb (replacements (. matcher group 0))))
    (. matcher appendTail sb)
    (.toString sb)))


(defn reduce-into
  "Same as (reduce into coll), but also works for empty collections and
   collections with 1 item. Elements of collection may be sets and maps"
  [coll]
  (cond (empty? coll) nil
	(= (count coll) 1) (first coll)
	true (reduce into coll)))

;;(defn flatten-1 [s]
;;  (reduce concat s))

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

;;(filter (fn [[buck rate]] (some #() (collect-parents buck 2)))

(defn collect-parents [buck depth]
  (cond
   (empty? (buck 'parents)) #{}
   (> depth 0) 
   (into (buck 'parents)
	 (reduce-into (map #(collect-parents % (- depth 1)) (buck 'parents))))
   true #{}))

(def COLLECT_PARENTS_DEPTH 1)

(defn filter-by-parents
  ([results] (filter-by-parents results 0.1))
  ([results c-min-rate]
     ;; (println (map #((first %) 'name) results))
     (let [rate-map (apply hash-map (flatten results))
	   ;;foo (println "rate-map = " rate-map)
	   ;;b (println "here")
	   filtered-buckets 
	   (doall (filter
		   (fn [buck]
		     (and (> (rate-map buck) 0.01) 
		      ;;(try
		      (some
		       #(> (rate-map %) c-min-rate)
		       (collect-parents buck COLLECT_PARENTS_DEPTH))
		      ;;(catch Exception e
		      ;; (println "!!! " (buck 'name))))
		      ))
		   (map first results)))
	   ;;baz (println "filtered-buckets = "
	   ;;     (map #(% 'name) filtered-buckets))
	   ;;boo (println "~~~~~")
	   needed-parents (flatten (map #(seq (collect-parents % 100))
					filtered-buckets))
	   ;;foo (println (map #(% 'name) needed-parents))
	   needed-buckets (distinct (concat needed-parents filtered-buckets))
	   ;;bar (println (map #(% 'name) needed-buckets))
	   ]
       (map (fn [buck] [buck (rate-map buck)])
	    needed-buckets))))


(defn find-buckets [root name]
  (cond (= (root 'name) name) #{ root }
	(empty? (root 'children)) #{}
	true (reduce-into (map #(find-buckets % name) (root 'children)))))


(defn find-bucket [root name]
  (let [ret (find-buckets root name)]
    (if (empty? ret)
      nil
      (first ret))))
