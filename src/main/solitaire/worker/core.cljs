(ns solitaire.worker.core
  (:require ["workbox-routing" :as wb-routing]
            ["workbox-strategies" :as wb-strategies]))

(defn destination-handler [dests f]
  (fn [opts]
    (let [request (.-request opts)]
      (when (contains? dests (.-destination request))
        (f)))))

(defn main []
  (wb-routing/registerRoute
    (destination-handler
      #{"image" "script" "style"}
      (fn []
        (wb-strategies/StaleWhileRevalidate.
          #js {:cacheName "stale"})))))
