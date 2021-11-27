(ns solitaire.worker.core
  (:require ["regenerator-runtime/runtime"]
            ["workbox-core" :as wb-core]
            ["workbox-routing" :as wb-routing]
            ["workbox-precaching" :as wb-precaching]
            ["workbox-strategies" :as wb-strategies]))

(goog-define base-url "")

(def cache-urls
  (->> ["/"
        "/assets/re-com/css/bootstrap.css"
        "/assets/re-com/css/material-design-iconic-font.min.css"
        "/assets/re-com/css/re-com.css"
        "/assets/re-com/css/chosen-sprite.png"
        "/assets/re-com/css/chosen-sprite@2x.png"
        "/assets/re-com/fonts/Material-Design-Iconic-Font.eot"
        "/assets/re-com/fonts/Material-Design-Iconic-Font.svg"
        "/assets/re-com/fonts/Material-Design-Iconic-Font.ttf"
        "/assets/re-com/fonts/Material-Design-Iconic-Font.woff"
        "/assets/re-com/fonts/Material-Design-Iconic-Font.woff2"
        "/assets/re-com/scripts/detect-element-resize.js"
        "/assets/css/app.css"
        "/assets/images/back.jpeg"
        "/assets/images/clubs.svg"
        "/assets/images/diamonds.svg"
        "/assets/images/hearts.svg"
        "/assets/images/spades.svg"
        "/assets/build/shared.js"
        "/assets/build/app.js"]
      (map #(str base-url %))
      (clj->js)))

(defn main []
  (.addEventListener
    js/self
    "install"
    (fn [event]
      (let [cache-name (.-runtime wb-core/cacheNames)]
        (-> (.waitUntil
              event
              (-> js/caches
                  (.open cache-name)
                  (.then #(.addAll % cache-urls))))))))

  (wb-routing/setDefaultHandler (wb-strategies/CacheFirst.))

  (wb-precaching/precacheAndRoute #js [#js {:url "/"}])

  (wb-routing/registerRoute
    (wb-routing/NavigationRoute. (wb-precaching/createHandlerBoundToURL "/"))))
