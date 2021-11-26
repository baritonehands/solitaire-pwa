(ns solitaire.game.events
  (:require [re-frame.core :refer [trim-v reg-event-fx inject-cofx]]
            [akiroz.re-frame.storage :as storage]
            [solitaire.game.db :refer [app-db]]))

(reg-event-fx
  :game/initialize
  [trim-v (inject-cofx :storage) (inject-cofx :undo-storage)]
  (fn [{:keys [storage undo-storage redo-storage] :as cofx} [from]]
    (if (and (= from :storage) storage)
      {:db         (merge-with merge app-db storage)
       :dispatch-n [[:drag/init]]
       :undo/reset [undo-storage redo-storage]}
      {:db         app-db
       :dispatch-n [[:drag/init]
                    [:deck/deal]]})))

