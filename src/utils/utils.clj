
(ns utils.utils
  (:import java.io.File))

(def caar (comp first first))
(def cadar (comp second first))

(defn list-dir [path]
  (map #(.getPath %) (.listFiles (File. path))))

(defn is-vector? [var]
  (instance? clojure.lang.PersistentVector var))

(defn is-map? [var]
  (instance? java.util.Map var))