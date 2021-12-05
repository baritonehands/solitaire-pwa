(ns solitaire.deck.events
  (:require [re-frame.core :refer [path trim-v reg-event-db reg-event-fx]]
            [akiroz.re-frame.storage :as storage]
            [day8.re-frame.undo :refer [undoable]]
            [solitaire.undo-storage :as undo-storage]
            [solitaire.deck :as deck]
            [solitaire.deck.rules :as rules]))

(def deck-path (path :deck))
(def persist-keys [:deck :settings])
(def persist-deck (storage/persist-db-keys :solitaire persist-keys))
(def persist-undo (undo-storage/persist-undo))

(def tableau-size (reduce + (range 1 8)))

(defn deal-impl [db _]
  (let [source (deck/create)
        tableau-source (take tableau-size source)
        stock (vec (drop tableau-size source))]
    (update
      db
      :deck
      merge
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
  [trim-v persist-deck persist-undo]
  deal-impl)

(reg-event-db
  :deck/draw
  [trim-v persist-deck persist-undo (undoable "draw card")] ; Note no deck-path
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
              :stock waste
              :waste [])
            (assoc deck
              :stock (drop to-take stock)
              :waste (concat
                       waste
                       (take to-take stock)))))))))

(reg-event-db
  :deck/draw-tableau
  [trim-v persist-deck persist-undo (undoable "draw tableau")]
  (fn [db [pile]]
    (update-in
      db
      [:deck :tableau pile]
      (fn [tableau]
        (if-let [card (last (:down tableau))]
          {:down (butlast (:down tableau))
           :up   [card]})))))

(reg-event-db
  :deck/draw-shortcut
  [deck-path trim-v]
  (fn [deck [path]]
    (let [stack (get-in deck path)
          {:keys [rank suit] :as card} (last stack)
          drop-path [:foundations (rules/suit->idx suit)]]
      (if (rules/legal-target? card deck drop-path)
        (-> deck
            (update-in path butlast)
            (update-in drop-path concat [card]))
        deck))))

(reg-event-db
  :deck/drag-start
  [deck-path trim-v]
  (fn [db [{:keys [path from-idx]} pos]]
    (let [stack (get-in db path)
          idx (or from-idx (dec (count stack)))
          cards (nthrest stack idx)]
      (-> db
          (assoc :dragging {:cards cards
                            :path  path
                            :idx   idx
                            :pos   pos})))))

(defn mobile? []
  (.-matches (.matchMedia js/window "(max-width: 725px)")))

(defn center-in-rect? [[x y] {:keys [top right bottom left]}]
  (let [width (if (mobile?) 45 100)
        height (if (mobile?) 60 150)
        cx (+ x (/ width 2))
        cy (+ y (/ height 2))]
    (and (>= cx left) (<= cx right)
         (>= cy top) (<= cy bottom))))

(defn droppable-target [[x y] targets]
  (->> (for [[path dims] targets
             :when (center-in-rect? [(- x js/window.scrollX)
                                     (- y js/window.scrollY)]
                                    dims)]
         path)
       (first)))

(reg-event-db
  :deck/drag-move
  [trim-v]                                                  ; Needs drag path too
  (fn [{:keys [deck drag] :as db} [x y]]
    (let [{:keys [targets]} drag]
      (-> db
          (update-in [:deck :dragging] assoc :pos [x y])
          (assoc-in [:deck :droppable] (droppable-target [x y] targets))))))

(reg-event-fx
  :deck/drag-end
  [deck-path trim-v]
  (fn [{{:keys [dragging droppable] :as deck} :db} _]
    (let [{:keys [cards path idx]} dragging
          to-path (if (and droppable
                           (rules/legal-target? (first cards) deck droppable))
                    droppable
                    path)]
      (-> {:db (-> deck
                   (assoc :dragging nil :droppable nil))}
          (cond-> (not= path to-path)
                  (assoc :dispatch [:deck/move-cards {:from-path path
                                                      :from-idx  idx
                                                      :to-path   to-path}]))))))

(reg-event-db
  :deck/move-cards
  [trim-v persist-deck persist-undo (undoable "move cards")]
  (fn [db [{:keys [from-path from-idx to-path]}]]
    (let [cards (->> (get-in db (cons :deck from-path))
                     (drop from-idx))]
      (-> db
          (update-in (cons :deck from-path) #(take from-idx %))
          (update-in (cons :deck to-path) concat cards)))))

(reg-event-fx
  :deck/force-store
  [persist-undo]
  (fn [db _]
    {:storage (select-keys db persist-keys)}))
