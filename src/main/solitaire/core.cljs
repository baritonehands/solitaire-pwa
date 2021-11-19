(ns solitaire.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [solitaire.components.card :as card]
            [solitaire.deck :as deck]))

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
  (let [deck (r/atom (deck/create))]
    (fn []
      [:<>
       [:h2 "Solitaire"]
       [:div
        [:button {:type "button"
                  :on-click (fn [] (swap! deck shuffle))}
         "Shuffle"]]
       [:div.deck
        (for [[idx card] (map-indexed vector @deck)]
          [card/view {:key  idx
                      :card card}])]])))

(defn main []
  (register-worker)
  (rdom/render [app-component] (.getElementById js/document "app-container")))