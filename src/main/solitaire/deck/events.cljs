(ns solitaire.deck.events
  (:require [re-frame.core :refer [path trim-v reg-event-db]]
            [solitaire.deck :as deck]))

(def deck-path (path :game :deck))

(def tableau-size (reduce + (range 1 8)))

(defn deal-impl [_ _]
  (let [source (deck/create)
        tableau-source (take tableau-size source)
        stock (vec (drop tableau-size source))]
    {:stock   stock
     :waste   []
     :foundations (vec (repeat 4 []))
     :tableau (loop [size 1
                     deck tableau-source
                     result []]
                (if (= size 8)
                  result
                  (recur
                    (inc size)
                    (drop size deck)
                    (assoc result (dec size) {:down (vec (take (dec size) deck))
                                              :up (-> (drop (dec size) deck) first vector)}))))}))

(reg-event-db
  :deck/deal
  [deck-path trim-v]
  deal-impl)

(def game-path (path :game))

(reg-event-db
  :deck/draw
  [game-path trim-v]
  (fn [{:keys [settings] :as db} _]
    (update
      db
      :deck
      (fn [{:keys [stock waste] :as deck}]
        (let [{:keys [num-cards]} settings
              remaining (count stock)
              to-take (min num-cards remaining)]
          (if (zero? remaining)
            (assoc deck
              :stock (vec (reverse waste))
              :waste [])
            (assoc deck
              :stock (drop to-take stock)
              :waste (concat
                       (take to-take stock)
                       waste))))))))

