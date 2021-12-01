(ns solitaire.header
  (:require [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [re-com.core :refer [box h-box title button md-icon-button]]
            [day8.re-frame.undo :as undo]))

(defn icon-button [& {:as args}]
  (-> args
      (assoc :style {:color "white"}
             :size :larger)
      (->>
        (mapcat identity)
        (into [md-icon-button]))))

(defn new-game []
  (undo/clear-history!)
  (dispatch [:game/initialize]))

(defn view []
  (let [redos? @(subscribe [:redos?])
        undos? @(subscribe [:undos?])]
    [h-box
     :size "none"
     :align :center
     :gap "10px"
     :children
     [[box
       :size "none"
       :child
       [title
        :label "Solitaire"
        :level :level2
        :style {:color  "white"
                :margin "20px 10px 10px 10px"}]]
      [box
       :size "none"
       :child [button
               :label [:span "New Game "
                       [:i {:className   "zmdi zmdi-plus"
                            :font-weight "bold"}]]
               :on-click (fn [_]
                           (dispatch [:dialog/confirm
                                      {:title         "Game in Progress"
                                       :text          "Would you like to start a new game?"
                                       :confirm-label "New Game"
                                       :cancel-label  "Cancel"
                                       :on-confirm    new-game}]))]]
      [h-box
       :size "1"
       :justify :end
       :gap "10px"
       :style {:margin-right "10px"}
       :children
       [[box
         :size "none"
         :child [icon-button
                 :md-icon-name "zmdi-undo"
                 :tooltip "Undo"
                 :disabled? (not undos?)
                 :on-click (fn []
                             (dispatch-sync [:undo])
                             (dispatch [:deck/force-store]))]]
        [box
         :size "none"
         :child [icon-button
                 :md-icon-name "zmdi-redo"
                 :tooltip "Redo"
                 :disabled? (not redos?)
                 :on-click (fn []
                             (dispatch-sync [:redo])
                             (dispatch [:deck/force-store]))]]]]]]))
