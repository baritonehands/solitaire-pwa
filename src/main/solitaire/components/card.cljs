(ns solitaire.components.card)

(defn view [{{:keys [rank suit]} :card}]
  [:div.card (str rank " of " suit)])
