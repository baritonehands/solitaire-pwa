(ns solitaire.workbox.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :workbox/show-prompt?
  (fn [db]
    (get-in db [:workbox :show-prompt?])))
