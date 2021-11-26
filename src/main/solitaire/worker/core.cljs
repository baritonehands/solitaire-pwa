(ns solitaire.worker.core
  (:require ["regenerator-runtime/runtime"]
            ["workbox-routing" :as wb-routing]
            ["workbox-strategies" :as wb-strategies]))

(defn destination-handler [dests]
  (fn [opts]
    (let [request (.-request opts)]
      (contains? dests (.-destination request)))))

(defn main []
  (wb-routing/registerRoute
    (destination-handler
      #{"image" "script" "style"})
    (wb-strategies/StaleWhileRevalidate.
      #js {:cacheName "stale"})))
