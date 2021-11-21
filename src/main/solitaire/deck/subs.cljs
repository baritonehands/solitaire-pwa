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

(reg-sub
  :deck/droppable
  (fn [db _]
    (get-in db [:deck :droppable])))
