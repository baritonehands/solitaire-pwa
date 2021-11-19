(ns solitaire.game.db
  (:require [solitaire.deck.db :as deck-db]))

(def app-db
  {:deck deck-db/db})
