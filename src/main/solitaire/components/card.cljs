(ns solitaire.components.card
  (:require [reagent.core :as r]
            [re-com.core :refer [box v-box label]]
            [solitaire.drag :as drag]
            [solitaire.utils :as utils]
            [solitaire.deck.rules :as rules]))

(defn blank [& [props & children]]
  (into
    [:div.card.blank (or props {})]
    children))

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
                :keys               [hidden? stacked? hz-stacked? on-drag-start on-drag-end]}]
  (r/with-let [*elem (atom nil)
               drag-opts (cond->
                           {}
                           (some? on-drag-start) (assoc :on-drag-start on-drag-start)
                           (some? on-drag-end) (assoc :on-drag-end on-drag-end))
               drag-handler (drag/mouse-down-handler *elem [:deck/drag-move] drag-opts)
               _ (.addEventListener js/document "touchstart" drag-handler #js {:passive false})
               _ (.addEventListener js/document "mousedown" drag-handler)]
    [:div.card
     (cond->
       {:className (cond-> (.toLowerCase suit)
                           hidden? (str " hidden")
                           stacked? (str " stacked")
                           hz-stacked? (str " hz-stacked"))
        :ref (fn [el]
               (when el
                 (reset! *elem el)))})

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
         :child [:div.corner-suit-image {:className (.toLowerCase suit)}]]]]]]
    (finally
      (.removeEventListener js/document "touchstart" drag-handler)
      (.removeEventListener js/document "mousedown" drag-handler))))

