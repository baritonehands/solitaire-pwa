(ns solitaire.deck.events
  (:require [re-frame.core :refer [path trim-v reg-event-db]]
            [solitaire.deck :as deck]))

(def deck-path (path :deck))

(def tableau-size (reduce + (range 1 8)))

(defn deal-impl [db _]
  (let [source (deck/create)
        tableau-source (take tableau-size source)
        stock (vec (drop tableau-size source))]
    (merge
      db
      {:stock       stock
       :waste       []
       :foundations (vec (repeat 4 []))
       :tableau     (loop [size 1
                           deck tableau-source
                           result []]
                      (if (= size 8)
                        result
                        (recur
                          (inc size)
                          (drop size deck)
                          (assoc result (dec size) {:down (vec (take (dec size) deck))
                                                    :up   (-> (drop (dec size) deck) first vector)}))))})))

(reg-event-db
  :deck/deal
  [deck-path trim-v]
  deal-impl)

(reg-event-db
  :deck/draw
  [trim-v] ; Note no deck-path
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

(reg-event-db
  :deck/drag-start
  [deck-path trim-v]
  (fn [db [card-path]]
    (let [stack (get-in db card-path)
          card (last stack)]
      (-> db
          (assoc :dragging {:card card
                            :path card-path})
          (update-in card-path butlast)))))

(reg-event-db
  :deck/drag-end
  [deck-path trim-v]
  (fn [{:keys [dragging] :as db} _]
    (let [{:keys [card path]} dragging]
      (-> db
          (assoc :dragging nil)
          (update-in path concat [card])))))
