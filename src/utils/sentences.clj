
(ns utils.sentences)

(defn word?
  "Word character.  These characters are lumped
   together into words."
  [c]
  (or (. Character isLetter c)
      (. Character isDigit c)
      (= \' c))) ; so that possessives work

(def keepers (set "\")]}"))

(defn keep?
  "Does c stick to the end of sentence after a '.'?"
  [c]
  (contains? keepers c))

(defn period?
  "A single period.  Can end a sentence, be a
   decimal place, or end an abbrev."
  [c]
  (= c \.))

(defn end?
  "A non-period end of sentence punctuation."
  [c]
  (or (= c \!)
      (= c \?)
      (= c \;))) ; Pygments now parses this correctly!

(defn space?
  "A space character (or hyphen).  These are ignored
   at the beginning of a sentence."
  [c]
  (or (= c \-)
      (= c \space)
      (= c \tab)
      (= c \newline)))


(defn string-from-vec
  "Create a string from a vec of characters."
  [vect]
  (apply str vect))

(defn build-strings
  "A vec of vecs of vecs of characters to a vec of
   vecs of strings."
  [sens]
  (map #(vec (map string-from-vec %)) sens))

(defn tokenize
  "Break a string into words organized as sentences.
   Returns a vec of vecs of strings"
  ([text]
     (tokenize 0 [] [] [] text))
  ([state sens sen word text]
     (let [h (first text)
           t (rest text)]
       (cond
        (= state 0)
        (cond
         (nil? h)
         (build-strings sens)
         (word? h)
         (recur 1 sens sen [h] t)
         (period? h)
         (recur 0 sens sen [] t)
         (end? h)
         (recur 0 sens sen [] t)
         (space? h)
         (recur 0 sens sen [] t)

         :default
         (recur 3 sens (conj sen [h]) [] t))

        (= state 1)
        (cond
         (nil? h)
         (build-strings (conj sens (conj sen word)))
         (word? h)
         (recur 1 sens sen (conj word h) t)
         (period? h)
         (recur 2 sens sen word t)
         (end? h)
         (recur 0 (conj sens (conj sen word [h])) [] [] t)
         (space? h)
         (recur 3 sens (conj sen word) [] t)

         :default
         (recur 3 sens (conj sen word [h]) [] t))

        (= state 2)
        (cond
         (nil? h)
         (build-strings (conj sens (conj sen word [\.])))
         (word? h)
         (recur 1 sens sen (conj word \. h) t)
         (period? h)
         (recur 2 sens sen word t)
         (end? h)
         (recur 0 (conj sens (conj sen (conj word [\.]) h)) [] [] t)
         (space? h)
         (recur 0 (conj sens (conj sen word [\.])) [] [] t)
         (keep? h)
         (recur 0 (conj sens (conj sen word [\.] [h])) [] [] t)

         :default
         (recur 0 (conj sens (conj sen word [\.])) [[h]] [] t))

        (= state 3)
        (cond
         (nil? h)
         (build-strings (conj sens sen))
         (word? h)
         (recur 1 sens sen [h] t)
         (period? h)
         (recur 2 sens sen [] t)
         (end? h)
         (recur 0 (conj sens (conj sen [h])) [] [] t)
         (space? h)
         (recur 3 sens sen [] t)

         :default
         (recur 3 sens (conj sen [h]) [] t))))))
    
