(ns solitaire.deck.component
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :refer [box h-box v-box label]]
            [solitaire.components.card :as card]
            [solitaire.drag :as drag]
            [solitaire.deck :as deck]
            [solitaire.deck.rules :as rules]))

(defn stock-view [{:keys [stock]}]
  [box
   :size "1"
   :justify :center
   :child [(if (empty? stock)
             card/blank
             card/face-down)
           {:on-click #(dispatch [:deck/draw])}]])

(defn waste-view [{:keys [waste]}]
  (let [{dragging-path :path} @(subscribe [:deck/dragging])
        drag-source? (= [:waste] dragging-path)
        size (count waste)
        idx (dec size)
        [behind card] (if (>= (count waste) 2)
                        (take-last 2 waste)
                        [nil (last waste)])]
    [h-box
     :size "2"
     :class "foundation-stack"
     :children
     [[box
       :size "1"
       :justify :center
       :child (if (some? behind)
                [card/face-up {:card behind}]
                [card/blank])]
      (if (some? card)
        [box
         :justify :center
         :child
         [card/face-up (cond-> {:card    card
                                :hidden? (and (= idx (dec size)) drag-source?)}
                               (= idx (dec size)) (assoc
                                                    :on-drag-start [:deck/drag-start {:path [:waste]}]
                                                    :on-drag-end [:deck/drag-end]))]])]]))

(defn foundations-view [{:keys [foundations]}]
  (let [{dragging-path :path} @(subscribe [:deck/dragging])]
    [h-box
     :size "4"
     :justify :between
     :children
     (for [idx (range 0 4)
           :let [suit (get deck/suits idx)
                 drag-source? (= [:foundations idx] dragging-path)
                 card (-> foundations (get idx) last)
                 behind (-> foundations (get idx) butlast last)
                 blank-component [card/blank {:className (.toLowerCase suit)}]
                 component [box
                            :child
                            (if (nil? card)
                              blank-component
                              [card/face-up {:card          card
                                             :hidden?       drag-source?
                                             :on-drag-start [:deck/drag-start {:path [:foundations idx]}]
                                             :on-drag-end   [:deck/drag-end]}])]]]
       (if (nil? card)
         ^{:key (str "empty" idx)}
         [drag/target [:foundations idx]
          :size "1"
          :justify :center
          :child component]
         [h-box
          :size "1"
          :class "foundation-stack"
          :children
          [[box
            :size "1"
            :justify :center
            :child (if (nil? behind)
                     blank-component
                     [card/face-up {:card behind}])]
           ^{:key (str "stacked" idx)}
           [drag/target [:foundations idx]
            :size "1"
            :justify :center
            :child component]]]))]))


(defn tableau-view [{:keys [tableau]}]
  (let [dragging @(subscribe [:deck/dragging])]
    [h-box
     :justify :between
     :align :start
     :children
     (for [[pile {:keys [down up]}] (map-indexed vector tableau)
           :let [{dragging-path  :path
                  dragging-cards :cards
                  dragging-idx   :idx} dragging
                 drag-source? (= [:tableau pile :up] dragging-path)
                 stack [v-box
                        :children
                        (concat
                          [(if (and (empty? down)
                                    (or
                                      (empty? up)
                                      (and drag-source? (= (count up) (count dragging-cards)))))
                             ^{:key "empty"}
                             [box
                              :child [card/blank]])]
                          (for [[idx _] (map-indexed vector down)
                                :let [last? (and (empty? up)
                                                 (= idx (dec (count down))))
                                      stacked? (or (< idx (dec (count down)))
                                                   (if drag-source?
                                                     (< (count dragging-cards) (count up))
                                                     (seq up)))]]
                            ^{:key (str "down" idx)}
                            [box
                             :child [card/face-down {:stacked? stacked?
                                                     :on-click (if last?
                                                                 #(dispatch [:deck/draw-tableau pile]))}]])
                          (for [[idx up-card] (map-indexed vector up)
                                :let [last? (= idx (dec (count up)))
                                      stacked? (if drag-source?
                                                 (< idx (dec dragging-idx))
                                                 (not last?))
                                      hidden? (and drag-source?
                                                   (>= idx dragging-idx))]]
                            [box
                             :child [card/face-up {:card            up-card
                                                   :stacked?        stacked?
                                                   :hidden?         hidden?
                                                   :on-drag-start   [:deck/drag-start {:path     [:tableau pile :up]
                                                                                       :from-idx idx}]
                                                   :on-drag-end     [:deck/drag-end]
                                                   :on-double-click (fn [_]
                                                                      (println "Double Click!")
                                                                      (dispatch [:deck/draw-shortcut [:tableau pile :up]]))}]]))]]]
       [drag/target [:tableau pile :up]
        :size "1 0 auto"
        :justify :center
        :child stack])]))

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
                :child [card/face-up {:card     card
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
       :justify :between
       :children
       [[stock-view {:stock stock}]
        [waste-view {:waste waste}]
        [foundations-view {:foundations foundations}]]]
      [tableau-view {:tableau tableau}]]]))
