(ns solitaire.dialog.events
  (:require [re-frame.core :refer [path trim-v reg-event-db]]))

(def db-path (path :dialog))

(reg-event-db
  :dialog/confirm
  [db-path trim-v]
  (fn [db [opts]]
    (assoc db :confirm opts)))

(reg-event-db
  :dialog/confirm-cancel
  [db-path trim-v]
  (fn [db _]
    (assoc db :confirm nil)))
