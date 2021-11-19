(ns solitaire.deck.component
  (:require [re-frame.core :refer [subscribe]]
            [re-com.core :refer [box h-box v-box]]
            [solitaire.components.card :as card]))

(defn stock-view [{:keys [stock]}]
  [box
   :size "1"
   :child [card/face-down {} (count stock)]])

(defn waste-view [{:keys [waste]}]
  [box
   :size "1"
   :child [card/blank]])

(defn foundations-view [{:keys [foundations]}]
  [h-box
   :size "5"
   :gap "5px"
   :children
   [[box :size "1" :child ""]
    [box :size "1" :child [card/blank]]
    [box :size "1" :child [card/blank]]
    [box :size "1" :child [card/blank]]
    [box :size "1" :child [card/blank]]]])

(defn tableau-view [{:keys [tableau]}]
  [h-box
   :gap "5px"
   :children
   (for [{:keys [down up]} tableau]
     [v-box
      :size "1 0 auto"
      :children
      (concat
        (for [[idx _] (map-indexed vector down)
              :let [stacked? (or (< idx (count down))
                                 (seq up))]]
          [box
           :child [card/face-down {:key      idx
                                   :stacked? stacked?}]])
        (for [[idx up-card] (map-indexed vector up)
              :let [stacked? (< idx (dec (count up)))]]
          [box
           :child [card/face-up {:key      idx
                                 :card     up-card
                                 :stacked? stacked?}]]))])])

(defn view []
  (let [{:keys [stock waste foundations tableau]} @(subscribe [:deck])]
    [v-box
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
