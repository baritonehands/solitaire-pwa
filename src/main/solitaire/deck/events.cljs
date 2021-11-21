(ns solitaire.deck.events
  (:require [re-frame.core :refer [path trim-v reg-event-db]]
            [solitaire.deck :as deck]
            [solitaire.deck.rules :as rules]))

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
  [trim-v]                                                  ; Note no deck-path
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
                       (reverse (take to-take stock))
                       waste))))))))

(reg-event-db
  :deck/draw-tableau
  [deck-path trim-v]
  (fn [deck [pile]]
    (update-in
      deck
      [:tableau pile]
      (fn [tableau]
        (if-let [card (last (:down tableau))]
          {:down (butlast (:down tableau))
           :up   [card]})))))

(reg-event-db
  :deck/drag-start
  [deck-path trim-v]
  (fn [db [card-path pos]]
    (let [stack (get-in db card-path)
          card (last stack)]
      (-> db
          (assoc :dragging {:card card
                            :path card-path
                            :pos  pos})
          (update-in card-path butlast)))))

(def card-width 100)
(def card-height 150)

(defn center-in-rect? [[x y] {:keys [top right bottom left]}]
  (let [cx (+ x (/ card-width 2))
        cy (+ y (/ card-height 2))]
    (and (>= cx left) (<= cx right)
         (>= cy top) (<= cy bottom))))

(defn droppable-target [[x y] targets]
  (->> (for [[path dims] targets
             :when (center-in-rect? [x y] dims)]
         path)
       (first)))

(reg-event-db
  :deck/drag-move
  [trim-v]                                                  ; Needs drag path too
  (fn [{:keys [deck drag] :as db} [x y]]
    (let [{{:keys [card path pos]} :dragging} deck
          {:keys [targets]} drag]
      (-> db
          (update-in [:deck :dragging] assoc :pos [x y])
          (assoc-in [:deck :droppable] (droppable-target pos targets))))))

(reg-event-db
  :deck/drag-end
  [deck-path trim-v]
  (fn [{:keys [dragging droppable] :as deck} _]
    (let [{:keys [card path]} dragging
          to-path (if (and droppable
                           (rules/legal-target? card deck droppable))
                    droppable
                    path)]
      (println dragging droppable)
      (-> deck
          (assoc :dragging nil :droppable nil)
          (update-in to-path concat [card])))))
