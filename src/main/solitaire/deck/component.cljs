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
   :child [(if (empty? stock)
             card/blank
             card/face-down)
           {:on-click #(dispatch [:deck/draw])}]])

(defn waste-view [{:keys [waste]}]
  (let [{dragging-path :path} @(subscribe [:deck/dragging])
        drag-source? (= [:waste] dragging-path)]
    [box
     :size "2"
     :child
     (if (empty? waste)
       [card/blank]
       (let [cards (take-last 3 waste)
             size (count cards)
             children (for [[idx card] (map-indexed vector cards)
                            :let [hz-stacked? (< idx
                                                 (if drag-source?
                                                   (- size 2)
                                                   (dec size)))]]
                        [box
                         :child [card/face-up (cond-> {:card        card
                                                       :hz-stacked? hz-stacked?
                                                       :hidden?     (and (= idx (dec size)) drag-source?)}
                                                      (= idx (dec size)) (assoc
                                                                           :on-drag-start [:deck/drag-start {:path [:waste]}]
                                                                           :on-drag-end [:deck/drag-end]
                                                                           :on-double-click [:deck/draw-shortcut [:waste]]))]])]
         [h-box
          :children
          (cons
            (if (and drag-source? (= size 1))
              [card/blank])
            children)]))]))

(defn foundations-view [{:keys [foundations]}]
  (let [{dragging-path :path} @(subscribe [:deck/dragging])]
    [h-box
     :size "4"
     :gap "5px"
     :children
     (for [idx (range 0 4)
           :let [suit (get deck/suits idx)
                 drag-source? (= [:foundations idx] dragging-path)
                 card (-> foundations (get idx) last)
                 behind (-> foundations (get idx) butlast last)
                 blank-component [card/blank {:className (.toLowerCase suit)}]
                 component (if (nil? card)
                             blank-component
                             [card/face-up {:card          card
                                            :hidden?       drag-source?
                                            :on-drag-start [:deck/drag-start {:path [:foundations idx]}]
                                            :on-drag-end   [:deck/drag-end]}])]]
       (if (nil? card)
         [drag/target [:foundations idx] :size "1" :child component]
         [h-box
          :size "1"
          :class "foundation-stack"
          :children
          [[box
            :child (if (nil? behind)
                     blank-component
                     [card/face-up {:card behind}])]
           [drag/target [:foundations idx] :size "1" :child component]]]))]))


(defn tableau-view [{:keys [tableau]}]
  (let [dragging @(subscribe [:deck/dragging])]
    [h-box
     :gap "5px"
     :children
     (for [[pile {:keys [down up]}] (map-indexed vector tableau)
           :let [{dragging-path  :path
                  dragging-cards :cards
                  dragging-idx   :idx} dragging
                 drag-source? (= [:tableau pile :up] dragging-path)]]
       [v-box
        :size "1 0 auto"
        :children
        (concat
          [(if (and (empty? down)
                    (or
                      (empty? up)
                      (and drag-source? (= (count up) (count dragging-cards)))))
             ^{:key "empty"}
             [drag/target [:tableau pile :up]
              :child [card/blank]])]
          (for [[idx _] (map-indexed vector down)
                :let [last? (and (empty? up)
                                 (= idx (dec (count down))))
                      stacked? (or (< idx (dec (count down)))
                                   (if drag-source?
                                     (< (count dragging-cards) (count up))
                                     (seq up)))]]
            (if last?
              ^{:key (str "down" idx)}
              [drag/target [:tableau pile :down]
               :child [card/face-down {:on-click #(dispatch [:deck/draw-tableau pile])}]]
              [box
               :child [card/face-down {:stacked? stacked?}]]))
          (for [[idx up-card] (map-indexed vector up)
                :let [last? (= idx (dec (count up)))
                      stacked? (if drag-source?
                                 (< idx (dec dragging-idx))
                                 (not last?))
                      hidden? (and drag-source?
                                   (>= idx dragging-idx))]]
            ^{:key (str "up" idx)}
            (if last?
              [drag/target [:tableau pile :up]
               :child [card/face-up {:card            up-card
                                     :hidden?         hidden?
                                     :on-drag-start   [:deck/drag-start {:path [:tableau pile :up]}]
                                     :on-drag-end     [:deck/drag-end]
                                     :on-double-click [:deck/draw-shortcut [:tableau pile :up]]}]]
              [box
               :child [card/face-up {:card            up-card
                                     :stacked?        stacked?
                                     :hidden?         hidden?
                                     :on-drag-start   [:deck/drag-start {:path     [:tableau pile :up]
                                                                         :from-idx idx}]
                                     :on-drag-end     [:deck/drag-end]
                                     :on-double-click [:deck/draw-shortcut [:tableau pile :up]]}]])))])]))

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
               ^{:key (str "overlay" idx)}
               [box
                :child [card/face-up {:card     card
                                      :class    "debug-overlay"
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
