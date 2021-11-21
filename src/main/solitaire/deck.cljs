(ns solitaire.deck)

(def suits
  ["Clubs"
   "Spades"
   "Hearts"
   "Diamonds"])

(def ranks
  (-> ["Ace"]
      (into (map str (range 2 11)))
      (into ["Jack" "Queen" "King"])))

(defn create []
  (->> (for [suit suits
             rank ranks]
         {:rank rank
          :suit suit})
       (shuffle)
       (vec)))
