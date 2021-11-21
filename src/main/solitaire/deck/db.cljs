(ns solitaire.deck.db
  (:require [solitaire.deck.subs]))

(def db
  {:stock       []
   :waste       []
   :foundations []
   :tableau     []
   :dragging    nil
   :droppable   nil})

