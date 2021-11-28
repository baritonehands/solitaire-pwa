(ns solitaire.workbox.events
  (:require [re-frame.core :refer [path trim-v reg-event-db reg-event-fx]]
            [solitaire.config :as config]
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

(reg-event-db
  :workbox/show-prompt
  [db-path trim-v]
  (fn [db [show-prompt?]]
    (assoc db :show-prompt? show-prompt?)))

(reg-event-fx
  :workbox/update
  [db-path trim-v]
  (fn [{:keys [db]} _]
    {:workbox.fx/update db}))
