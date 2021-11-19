(ns solitaire.game.events
  (:require [re-frame.core :refer [path trim-v reg-event-fx]]
            [solitaire.game.db :refer [app-db]]))

(def db-path (path :game))

(reg-event-fx
  :game/initialize
  [db-path trim-v]
  (fn [_ _]
    {:db       app-db
     :dispatch [:deck/deal]}))
