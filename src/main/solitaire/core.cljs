(ns solitaire.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-com.core :refer [box h-box v-box title button]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [solitaire.deck.component :as deck]
            [solitaire.drag :as drag]
            [solitaire.events]))

(defn register-worker []
  (if (js-in "serviceWorker" js/navigator)
    (.addEventListener js/window "load"
                       (fn []
                         (..
                           js/navigator
                           -serviceWorker
                           (register "/assets/build/worker.js")
                           (then
                             (fn [_]
                               (println "Registered!"))
                             (fn [error]
                               (println "ServiceWorker registration failed:" error))))))
    (println "ServiceWorker is not supported")))

(defn app-component []
  [:<>
   [v-box
    :style {:position "relative"}
    :children
    [[h-box
      :size "none"
      :align :center
      :gap "10px"
      :children
      [[box
        :size "none"
        :child [title :label "Solitaire" :level :level2]]
       [box
        :size "none"
        :child [button :label "Reset" :on-click (fn [] (dispatch [:game/initialize]))]]]]
     [box :size "auto" :child [deck/view]]]]
   [deck/overlay]])

(defn ^:dev/after-load force-rerender []
  (rdom/force-update-all))

(defn main []
  (register-worker)
  (dispatch-sync [:game/initialize])
  (rdom/render [app-component] (.getElementById js/document "app-container")))