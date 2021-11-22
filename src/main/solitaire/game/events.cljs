(ns solitaire.game.events
  (:require [re-frame.core :refer [trim-v reg-event-fx]]
            [solitaire.game.db :refer [app-db]]))

(reg-event-fx
  :game/initialize
  [trim-v]
  (fn [_ _]
    {:db       app-db
     :dispatch-n [[:drag/init]
                  [:deck/deal]]}))

