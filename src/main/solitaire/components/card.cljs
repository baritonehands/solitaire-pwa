(ns solitaire.components.card
  (:require [reagent.core :as r]
            [re-com.core :refer [box v-box label]]
            [solitaire.drag :as drag]
            [solitaire.utils :as utils]
            [solitaire.deck.rules :as rules]))

(defn blank [& [props]]
  [:div.card.blank (or props {})])

(defn face-down [{:keys [stacked? on-click]} & children]
  (into
    [:div.card.back
     {:className (cond-> ""
                         stacked? (str " stacked")
                         on-click (str " clickable"))
      :on-click  on-click}]
    children))

(defn short-rank [rank]
  (if (= rank "10")
    "10"
    (first rank)))

(defn face-up [{{:keys [rank suit]} :card
                :keys               [stacked? hz-stacked? on-drag-start on-drag-end on-double-click]}]
  (let [*bounds (r/atom {:x 0 :y 0})
        drag-opts (cond->
                    {}
                    (some? on-drag-start) (assoc :on-drag-start on-drag-start)
                    (some? on-drag-end) (assoc :on-drag-end on-drag-end)
                    (some? on-double-click) (assoc :on-double-click on-double-click))]
    (fn [{{:keys [rank suit]} :card
          :keys               [stacked? hz-stacked? on-drag-start on-drag-end on-double-click]}]
      (let [{:keys [x y]} @*bounds]
        [:div.card
         (cond->
           {:className (cond-> (.toLowerCase suit)
                               stacked? (str " stacked")
                               hz-stacked? (str " hz-stacked"))
            :ref       (fn [el]
                         (when el
                           (let [bounds (-> (.getBoundingClientRect el)
                                            (.toJSON)
                                            (js->clj :keywordize-keys true))]
                             (swap!
                               *bounds
                               (fn [old-bounds]
                                 (if (not= old-bounds bounds)
                                   bounds
                                   old-bounds))))))}
           (seq drag-opts) (assoc :on-mouse-down (drag/mouse-down-handler [x y] [:deck/drag-move] drag-opts)))
         [:div.corner
          [v-box
           :align :center
           :children
           [[label
             :style {:color (rules/color suit)}
             :class "corner-label"
             :label (short-rank rank)]
            [box
             :size "none"
             :class "corner-suit"
             :child [:img {:src (str "assets/images/" (.toLowerCase suit) ".svg")
                           :alt suit}]]]]]]))))
