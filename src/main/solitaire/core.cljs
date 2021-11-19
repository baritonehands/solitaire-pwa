(ns solitaire.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [solitaire.deck.component :as deck]
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
   [:h2 "Solitaire"]
   [:div
    [:button {:type "button"
              :on-click (fn [] (dispatch [:game/initialize]))}
     "Reset"]]
   [deck/view]])

(defn ^:dev/after-load force-rerender []
  (rdom/force-update-all))

(defn main []
  (register-worker)
  (dispatch-sync [:game/initialize])
  (rdom/render [app-component] (.getElementById js/document "app-container")))