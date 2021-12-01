(ns solitaire.workbox.events
  (:require [re-frame.core :refer [path trim-v reg-event-db reg-event-fx]]
            [solitaire.workbox.fx]))

(def db-path (path :workbox))

(reg-event-fx
  :workbox/init
  [db-path trim-v]
  (fn [_ _]
    {:workbox.fx/init true}))

(reg-event-db
  :workbox/set-instance
  [db-path trim-v]
  (fn [db [instance]]
    (assoc db :instance instance)))

(reg-event-fx
  :workbox/update
  [db-path trim-v]
  (fn [{:keys [db]} _]
    {:workbox.fx/update db}))
