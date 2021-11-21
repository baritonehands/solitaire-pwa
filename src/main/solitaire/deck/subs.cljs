(ns solitaire.deck.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :deck
  (fn [db _]
    (get-in db [:deck])))

(reg-sub
  :deck/dragging
  (fn [db _]
    (get-in db [:deck :dragging])))

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

(reg-sub
  :deck/droppable
  (fn [{:keys [deck drag]} _]
    (let [{{:keys [pos]} :dragging} deck
          {:keys [targets]} drag]
      (droppable-target pos targets))))
