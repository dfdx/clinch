
(ns clinch.spamfilter
  (:use [clojure.contrib def]
	[clinch core]))


(defstruct 
    #^{:doc "Struct to keep filter state. "}
  spam-filter :index :vbox :bucket-row :spam-text :non-spam-text)

(defnk make-spam-filter
  "Creates spam filter object. "
  [spam-text non-spam-text :path nil :analyzer nil]
  (let [idx (if path (make-index path) (make-index))
	vbox (make-vector-box idx)
	an (if analyzer analyzer (auto-analyzer non-spam-text))]
    (with-meta
      (struct-map spam-filter
	:index idx
	:vbox vbox
	:bucket-row (make-bucket-row idx vbox {"spam" spam-text
					       "non-spam" non-spam-text}
				     an)
	:spam-text spam-text 
	:non-spam-text non-spam-text)
      {:type ::spam-filter})))


(defn check [sf text]
  (let [found (find-best-in-row (:bucket-row sf) (:vbox sf) text)]
    [(:name (first found)) (second found)]))