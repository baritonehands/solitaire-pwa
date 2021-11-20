(ns solitaire.deck.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :deck
  (fn [db _]
    (get-in db [:deck])))
