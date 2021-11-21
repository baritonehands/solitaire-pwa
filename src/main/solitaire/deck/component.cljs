(ns solitaire.deck.component
  (:require [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :refer [box h-box v-box label]]
            [solitaire.components.card :as card]
            [solitaire.drag :as drag]
            [solitaire.deck :as deck]
            [solitaire.deck.rules :as rules]))

(defn stock-view [{:keys [stock]}]
  [box
   :size "1"
   :child [card/face-down
           {:on-click #(dispatch [:deck/draw])}
           (count stock)]])

(defn waste-view [{:keys [waste]}]
  [box
   :size "2"
   :child
   (if (empty? waste)
     [card/blank]
     (let [cards (take-last 3 waste)]
       [h-box
        :children
        (for [[idx card] (map-indexed vector cards)
              :let [size (count cards)
                    hz-stacked? (< idx (dec size))]]
          [box
           :child [card/face-up (cond-> {:key         idx
                                         :card        card
                                         :hz-stacked? hz-stacked?}
                                        (= idx (dec size)) (assoc
                                                             :on-drag-start [:deck/drag-start {:path [:waste]}]
                                                             :on-drag-end [:deck/drag-end]))]])]))])

(defn foundations-view [{:keys [foundations]}]
  [h-box
   :size "4"
   :gap "5px"
   :children
   (for [idx (range 0 4)
         :let [suit (get deck/suits idx)
               component (if-let [card (-> foundations (get idx) last)]
                           [card/face-up {:card          card
                                          :on-drag-start [:deck/drag-start {:path [:foundations idx]}]
                                          :on-drag-end   [:deck/drag-end]}]
                           [card/blank
                            [label
                             :style {:color (rules/color suit)}
                             :label suit]])]]
     [drag/target [:foundations idx] :size "1" :child component])])

(defn tableau-view [{:keys [tableau]}]
  [h-box
   :gap "5px"
   :children
   (for [[pile {:keys [down up]}] (map-indexed vector tableau)]
     [v-box
      :size "1 0 auto"
      :children
      (concat
        (if (and (empty? down) (empty? up))
          [[drag/target [:tableau pile :up]
            :child [card/blank]]]
          [])
        (for [[idx _] (map-indexed vector down)
              :let [stacked? (or (< idx (dec (count down)))
                                 (seq up))]]
          (if stacked?
            [box
             :child [card/face-down {:key      idx
                                     :stacked? stacked?}]]
            [drag/target [:tableau pile :down]
             :child [card/face-down {:key      idx
                                     :on-click #(dispatch [:deck/draw-tableau pile])}]]))
        (for [[idx up-card] (map-indexed vector up)
              :let [stacked? (< idx (dec (count up)))]]
          (if stacked?
            [box
             :child [card/face-up {:key           idx
                                   :card          up-card
                                   :stacked?      true
                                   :on-drag-start [:deck/drag-start {:path     [:tableau pile :up]
                                                                     :from-idx idx}]
                                   :on-drag-end   [:deck/drag-end]}]]
            [drag/target [:tableau pile :up]
             :child [card/face-up {:key           idx
                                   :card          up-card
                                   :on-drag-start [:deck/drag-start {:path [:tableau pile :up]}]
                                   :on-drag-end   [:deck/drag-end]}]])))])])

(defn overlay []
  (if-let [{:keys [cards pos]} @(subscribe [:deck/dragging])]
    [box
     :style {:left (first pos)
             :top  (second pos)}
     :class "drag-overlay"
     :child [v-box
             :size "1 0 auto"
             :children
             (for [[idx card] (map-indexed vector cards)
                   :let [stacked? (< idx (dec (count cards)))]]
               [box
                :child [card/face-up {:key      idx
                                      :card     card
                                      :stacked? stacked?}]])]]))


(defn view []
  (let [{:keys [stock waste foundations tableau]} @(subscribe [:deck])]
    [v-box
     :style {:overflow "visible"}
     :margin "10px"
     :gap "10px"
     :children
     [[h-box
       :size "auto"
       :gap "5px"
       :children
       [[stock-view {:stock stock}]
        [waste-view {:waste waste}]
        [foundations-view {:foundations foundations}]]]
      [tableau-view {:tableau tableau}]]]))
