(ns solitaire.components.card
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
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

(defn debug-elem [elem]
  (.-outerHTML elem))

(defn face-up [{{:keys [rank suit]} :card
                :keys               [on-drag-start on-drag-end on-double-click]}]
  (let [*elem (atom nil)
        drag-opts (cond->
                    {}
                    (some? on-drag-start) (assoc :on-drag-start on-drag-start)
                    (some? on-drag-end) (assoc :on-drag-end on-drag-end)
                    (some? on-double-click) (assoc :on-double-click on-double-click))
        drag-handler (drag/mouse-down-handler *elem [:deck/drag-move] drag-opts)]
    (add-watch *elem "card-face" (fn [_ _ old-state new-state]
                                   (println (some-> old-state .-outerHTML)
                                            "\n\n"
                                            (some-> new-state .-outerHTML)
                                            "\n"
                                            (= old-state new-state))))

    (r/create-class
      {:component-did-mount
       (fn [this]
         (.addEventListener js/document "touchstart" drag-handler #js {:passive false})
         (.addEventListener js/document "mousedown" drag-handler))

       :component-will-unmount
       (fn [this]
         (.removeEventListener js/document "touchstart" drag-handler)
         (.removeEventListener js/document "mousedown" drag-handler))

       :reagent-render
       (fn [{{:keys [rank suit]} :card
             :keys               [hidden? stacked? hz-stacked? class]}]
         [:div.card
          (cond->
            {:className       (cond-> (.toLowerCase suit)
                                      class (str " " class)
                                      hidden? (str " hidden")
                                      stacked? (str " stacked")
                                      hz-stacked? (str " hz-stacked"))
             :ref             (fn [el]
                                (when (and el (not= el @*elem))
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
              :child [:div.corner-suit-image {:className (.toLowerCase suit)}]]]]]])})))

