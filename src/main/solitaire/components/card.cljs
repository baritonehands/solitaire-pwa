(ns solitaire.components.card
  (:require [re-com.core :refer [box]]))

(defn blank []
  [:div.card])

(defn face-down [{:keys [stacked?]} & children]
  (into
    [:div.card
     {:className (and stacked? "stacked")}]
    children))

(defn face-up [{{:keys [rank suit]} :card
                stacked?            :stacked?}]
  [:div.card
   {:className (and stacked? "stacked")}
   (str rank " of " suit)])
