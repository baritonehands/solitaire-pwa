(ns solitaire.workbox.fx
  (:require [re-frame.core :refer [dispatch reg-fx]]
            [solitaire.config :as config]
            ["workbox-window" :as wb-window]))

(def update-text "A new version of Solitaire is available. Click \"Update\" to update to the latest version.")

(defn open-update-dialog-handler [wb]
  (fn [_]
    (dispatch [:dialog/confirm
               {:title         "Update Available"
                :text          update-text
                :confirm-label "Update"
                :cancel-label  "Dismiss"
                :on-confirm    [:workbox/update wb]}])))

(reg-fx
  :workbox.fx/init
  (fn [_]
    (if (js-in "serviceWorker" js/navigator)
      (let [wb (wb-window/Workbox. "worker.js" #js {:scope (str config/base-url "/")})]
        (.addEventListener wb "waiting" (open-update-dialog-handler wb))
        (.register wb)
        (dispatch [:workbox/set-instance wb]))
      (println "ServiceWorker is not supported"))))

(reg-fx
  :workbox.fx/update
  (fn [{:keys [instance]}]
    (.addEventListener instance "controlling" (fn [_]
                                                (.reload js/window.location)))
    (.messageSkipWaiting instance)))

