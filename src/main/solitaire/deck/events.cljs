(ns solitaire.deck.events
  (:require [re-frame.core :refer [path trim-v reg-event-db]]
            [solitaire.deck :as deck]))

(def db-path (path :game :deck))

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
  [db-path]
  deal-impl)




