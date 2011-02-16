
(ns clinch.utils
  (:import java.io.File
	   org.apache.lucene.analysis.snowball.SnowballAnalyzer
	   org.apache.lucene.util.Version))


(defn reducable [f]
  "Makes reducable version of 2-args functions. E.g. you can't do
     (reduce conj '())
   but you can do
     (def rconj (reducable conj))
     (reduce rconj '())"
  (fn [& args]
    (cond (empty? args) args
	  :else (reduce f args))))


(def rconj (reducable conj))
(def rinto (reducable into))


(def caar (comp first first))
(def cadar (comp second first))


(defn list-dir [path]
  (map #(.getPath %) (.listFiles (File. path))))


(defn is-vector? [var]
  (instance? clojure.lang.PersistentVector var))

(defn is-map? [var]
  (instance? java.util.Map var))


(defn assert-type [var t]
  (= (type var) t))
