(ns solitaire.game.db
  (:require [solitaire.deck.db :as deck-db]
            [solitaire.settings.db :as settings-db]
            [solitaire.drag.db :as drag-db]
            [solitaire.workbox.db :as workbox-db]
            [solitaire.dialog.db :as dialog-db]))

(def app-db
  {:deck     deck-db/db
   :settings settings-db/db
   :drag     drag-db/db
   :workbox  workbox-db/db
   :dialog   dialog-db/db})
