(ns solitaire.deck)

(def suits
  ["Spades"
   "Clubs"
   "Diamonds"
   "Hearts"])

(def ranks
  (-> ["Ace"]
      (into (map str (range 2 11)))
      (into ["Jack" "King" "Queen"])))

(defn create []
  (->> (for [suit suits
             rank ranks]
         {:rank rank
          :suit suit})
       (shuffle)
       (vec)))
