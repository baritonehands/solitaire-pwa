(ns solitaire.drag.events
  (:require [re-frame.core :refer [path trim-v reg-event-db reg-event-fx]]
            [solitaire.drag.fx]))

(def db-path (path :drag))

(reg-event-fx
  :drag/start
  [db-path trim-v]
  (fn [_ [opts]]
    {:drag.fx/start opts}))

(reg-event-db
  :drag/set-target
  [db-path trim-v]
  (fn [db [key bounds]]
    (assoc-in db [:targets key] bounds)))

(reg-event-db
  :drag/remove-target
  [db-path trim-v]
  (fn [db [key]]
    (update db :targets dissoc key)))
