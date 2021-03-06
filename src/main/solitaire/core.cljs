(ns solitaire.core
  (:require [reagent.dom :as rdom]
            [re-com.core :refer [box h-box v-box title button]]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [solitaire.deck.component :as deck]
            [solitaire.drag.components :as drag]
            [solitaire.dialog.confirm :as confirm]
            [solitaire.header :as header]
            [solitaire.footer :as footer]
            [solitaire.events]))

(defn app-component []
  [:<>
   [drag/debug-underlay]
   [v-box
    :style {:position "relative"}
    :children
    [[header/view]
     [box :size "auto" :child [deck/view]]
     [footer/view]]]
   [deck/overlay]
   [confirm/view]])

(defn ^:dev/after-load force-rerender []
  (rdom/force-update-all))

(defn ^:export main []
  (dispatch-sync [:game/initialize :storage])
  (rdom/render [app-component] (.getElementById js/document "app-container")))