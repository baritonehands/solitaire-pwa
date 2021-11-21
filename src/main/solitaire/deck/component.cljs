(ns solitaire.deck.component
  (:require [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :refer [box h-box v-box]]
            [solitaire.components.card :as card]
            [solitaire.drag :as drag]))

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
     [h-box
      :children
      (for [[idx card] (map-indexed vector (take 3 waste))
            :let [size (count (take 3 waste))
                  hz-stacked? (< idx (dec size))]]
        [box
         :child [card/face-up (cond-> {:key         idx
                                       :card        card
                                       :hz-stacked? hz-stacked?}
                                      (= idx (dec size)) (assoc
                                                           :on-drag-start [:deck/drag-start [:waste]]
                                                           :on-drag-end   [:deck/drag-end]))]])])])

(defn draggable-box []
  [box :size "1" :child [card/blank]])

(defn foundations-view [{:keys [foundations]}]
  [h-box
   :size "4"
   :gap "5px"
   :children
   (for [idx (range 0 4)
         :let [component (if-let [card (-> foundations (get idx) last)]
                           [card/face-up {:card card}]
                           [card/blank])]]
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
             :child [card/face-up {:key      idx
                                   :card     up-card
                                   :stacked? true}]]
            [drag/target [:tableau pile :up]
             :child [card/face-up {:key           idx
                                   :card          up-card
                                   :on-drag-start [:deck/drag-start [:tableau pile :up]]
                                   :on-drag-end   [:deck/drag-end]}]])))])])

(defn overlay []
  (if-let [{:keys [card pos]} @(subscribe [:deck/dragging])]
    [box
     :style {:left (first pos)
             :top  (second pos)}
     :class "drag-overlay"
     :child [card/face-up {:card card}]]))


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
