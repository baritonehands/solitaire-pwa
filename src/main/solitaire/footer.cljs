(ns solitaire.footer
  (:require [re-com.core :refer [box hyperlink-href]]
            [re-frame.db :refer [app-db]]
            [goog.crypt.base64 :as base64]
            [akiroz.re-frame.storage :as storage]))

(defn email-body []
  (-> {:state   (select-keys @app-db [:deck :settings])
       :storage (storage/<-store :solitaire)}
      (pr-str)
      (base64/encodeString base64/Alphabet.WEBSAFE)))

(defn view []
  [box
   :child
   [hyperlink-href
    :label "Report an Issue"
    :target "_blank"
    :style {:color "white"}
    :href (str "mailto:baritonehands@gmail.com?subject=Issue+With+Solitaire&body="
               (.encodeURIComponent js/window "Description of issue:\n\n\nApplication state (do not modify):\n")
               (email-body))]])
