(ns solitaire.drag.components
  (:require [re-frame.core :refer [subscribe]]))

(defn debug-underlay []
  (if (true? js/goog.DEBUG)
    (let [targets @(subscribe [:drag/targets])]
      [:div.debug-underlay
       (for [[path {:keys [top right bottom left width height]}] targets]
         ^{:key path}
         [:div.drag-target
          {:style {:top    (str top "px")
                   :left   (str left "px")
                   :width  (str width "px")
                   :height (str height "px")}}
          (str path)])])))
