
(ns semantic.vectors
  (:use utils.utils clojure.contrib.math))

;; randoms

(defn random+-1 []
  (if (= (rand-int 2) 1) 1 -1))

(defn make-unique-randoms [n max]
  (take n (distinct (repeatedly #(rand-int max)))))

(defn make-vector [_] 
  [])

(defn make-random-vector [dim seed]
  (let [positions-seq (make-unique-randoms seed dim)]
    (map (fn [pos] [pos (random+-1)]) (sort positions-seq))))

(def cur-pos caar)
(def cur-val cadar)

(defn add [v1 v2]
  (loop [vec1 v1 vec2 v2 res (make-vector 'foo)]
    (cond 
     (empty? vec1) (vec (concat res vec2))
     (empty? vec2) (vec (concat res vec1))
     (< (cur-pos vec1) (cur-pos vec2)) (recur (rest vec1) vec2
					      (conj res (first vec1)))
     (< (cur-pos vec2) (cur-pos vec1)) (recur vec1 (rest vec2)
					      (conj res (first vec2)))
     true (recur (rest vec1) (rest vec2) (conj res [(cur-pos vec1)
						    (+ (cur-val vec1)
						       (cur-val vec2))])))))

(defn minus [v]
  (vec (map (fn [el] [(first el) (- (second el))]) v)))

(defn sub [v1 v2]
  (vec (filter #(not= (second %) 0) (add v1 (minus v2)))))

;;(defn abs [arg] (if (>= arg 0) arg (- arg)))

(defn mult-scalar [v1 v2]
  (loop [vec1 v1 vec2 v2 res (make-vector 'foo)]
    (cond 
     (or (empty? vec1) (empty? vec2)) (reduce
				       #(+ %1 (second %2)) 0
					      res) 
     (< (cur-pos vec1) (cur-pos vec2)) (recur (rest vec1) vec2 res)
     (< (cur-pos vec2) (cur-pos vec1)) (recur vec1 (rest vec2) res)
     true (recur (rest vec1) (rest vec2) (conj res [(cur-pos vec1)
						    (* (cur-val vec1)
						       (cur-val vec2))])))))

(defn vector-length [v]
  (Math/sqrt (mult-scalar v v)))

(defn cosine [v1 v2]
  (let [multiplied (* (vector-length v1) (vector-length v2))]
    (if (not= multiplied 0)
      (/ (mult-scalar v1 v2) multiplied)
      0)))

(defn remove-zero-elements [v]
  (filter #(not= (second %) 0) v))

(defn mult-const [v c]
  (remove-zero-elements (map (fn [el] [(first el) (* (second el) c)]) v)))
