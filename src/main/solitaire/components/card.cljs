(ns solitaire.components.card
  (:require [re-com.core :refer [box]]))

(defn blank []
  [:div.card])

(defn face-down [{:keys [stacked? on-click]} & children]
  (into
    [:div.card
     {:className (cond-> ""
                         stacked? (str " stacked")
                         on-click (str " clickable"))
      :on-click  on-click}]
    children))

(defn face-up [{{:keys [rank suit]} :card
                :keys               [stacked? hz-stacked?]}]
  [:div.card
   {:className (cond-> ""
                       stacked? (str "stacked")
                       hz-stacked? (str " hz-stacked"))}
   (str rank " of " suit)])
