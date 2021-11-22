(ns solitaire.deck.rules
  (:require [solitaire.deck :as deck]))

(defn color [suit]
  (case suit
    ("Clubs" "Spades") "black"
    "#d40000"))

(def rank->idx
  (->> (map vector deck/ranks (range))
       (into {})))

(def suit->idx
  (->> (map vector deck/suits (range))
       (into {})))

(defn legal-target? [{:keys [rank suit]} deck drop-path]
  (let [pile (get-in deck drop-path)
        last-card (last pile)]
    (case (first drop-path)
      :tableau
      (let [dir (last drop-path)]
        (case dir
          :down false
          :up (or (and (nil? last-card) (= rank "King"))
                  (and (not= (color suit) (color (:suit last-card)))
                       (= (rank->idx rank) (dec (rank->idx (:rank last-card))))))))

      :foundations
      (and (= (suit->idx suit) (second drop-path))
           (cond
             (empty? pile) (and (zero? (rank->idx rank)))
             :else (= (rank->idx rank) (inc (rank->idx (:rank last-card)))))))))


