(ns solitaire.components.card
  (:require [reagent.core :as r]
            [re-com.core :refer [box]]
            [solitaire.drag :as drag]
            [solitaire.utils :as utils]))

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

(defn face-up [_]
  (let [*bounds (r/atom {:x 0 :y 0})]
    (fn [{{:keys [rank suit]} :card
          :keys               [stacked? hz-stacked? on-drag-start on-drag-end]}]
      (let [{:keys [x y]} @*bounds
            drag-opts (cond->
                        {}
                        (some? on-drag-start) (assoc :on-drag-start on-drag-start)
                        (some? on-drag-end) (assoc :on-drag-end on-drag-end))]

        [:div.card
         (cond->
           {:className (cond-> ""
                               stacked? (str "stacked")
                               hz-stacked? (str " hz-stacked"))
            :ref       (fn [el]
                         (when el
                           (reset! *bounds (-> (.getBoundingClientRect el)
                                               (.toJSON)
                                               (js->clj :keywordize-keys true)))))}
           (seq drag-opts) (assoc :on-mouse-down
                                  (drag/mouse-down-handler [x y] println drag-opts)))
         (str rank " of " suit)]))))
