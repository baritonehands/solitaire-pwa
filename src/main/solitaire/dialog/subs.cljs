(ns solitaire.dialog.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :dialog/confirm
  (fn [db]
    (get-in db [:dialog :confirm])))