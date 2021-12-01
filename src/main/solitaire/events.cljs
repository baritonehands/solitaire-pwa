(ns solitaire.events
  (:require [akiroz.re-frame.storage :as storage]
            [day8.re-frame.undo :as undo]
            [solitaire.undo-storage]
            [solitaire.deck.events]
            [solitaire.game.events]
            [solitaire.drag.events]
            [solitaire.workbox.events]
            [solitaire.dialog.events]))

(storage/reg-co-fx! :solitaire {:fx   :storage
                                :cofx :storage})

(undo/undo-config!
  {:max-undos 2000
   :harvest-fn #(-> % deref (select-keys [:deck]))
   :reinstate-fn (fn [ratom value]
                   (swap! ratom merge value))})

