(ns solitaire.workbox.fx
  (:require [re-frame.core :refer [dispatch reg-fx]]
            [solitaire.config :as config]
            ["workbox-window" :as wb-window]))

(reg-fx
  :workbox.fx/init
  (fn [_]
    (if (js-in "serviceWorker" js/navigator)
      (let [wb (wb-window/Workbox. "worker.js" #js {:scope (str config/base-url "/")})]
        (.addEventListener wb "waiting" #(dispatch [:workbox/show-prompt true]))
        (.register wb)
        (dispatch [:workbox/set-instance wb]))
      (println "ServiceWorker is not supported"))))

(reg-fx
  :workbox.fx/update
  (fn [{:keys [instance]}]
    (.addEventListener instance "controlling" (fn [_]
                                                (.reload js/window.location)))
    (.messageSkipWaiting instance)))

