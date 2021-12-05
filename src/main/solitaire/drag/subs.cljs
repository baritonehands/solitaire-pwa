(ns solitaire.drag.subs
  (:require [re-frame.core :refer [reg-sub]]))

; For debugging purposes
(reg-sub
  :drag/targets
  (fn [db]
    (get-in db [:drag :targets])))
