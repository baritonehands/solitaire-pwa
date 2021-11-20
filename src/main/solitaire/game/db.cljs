(ns solitaire.game.db
  (:require [solitaire.deck.db :as deck-db]
            [solitaire.settings.db :as settings-db]))

(def app-db
  {:deck     deck-db/db
   :settings settings-db/db})
