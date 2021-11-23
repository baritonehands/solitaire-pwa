(ns solitaire.drag.fx
  (:require [re-frame.core :refer [reg-fx dispatch]]
            [goog.events :as events])
  (:import [goog.events EventType]))

(reg-fx
  :drag.fx/init
  (fn [_]
    (events/listen js/document EventType.TOUCHMOVE #(.preventDefault %) #js {:passive false})
    (events/listen js/document "touchforcechange" #(.preventDefault %) #js {:passive false})))

(defn scale [n px]
  (int (/ n px)))

(defn new-pos [[ox oy] [x y] e opts]
  (let [sign (if (:invert? opts) - +)
        px (get opts :scale 1)]
    [(-> (.-clientX e) (- ox) (scale px) sign (+ x) int)
     (-> (.-clientY e) (- oy) (scale px) sign (+ y) int)]))

(defn event-or-fn [ev-or-f new-pos]
  (if (fn? ev-or-f)
    (ev-or-f new-pos)
    (dispatch (into ev-or-f new-pos))))

(reg-fx
  :drag.fx/start
  (fn [{:keys [offset start event opts]}]
    (let [{:keys [on-drag-start on-drag-end]} opts
          on-move (fn [e]
                    (event-or-fn event (new-pos offset start e opts))
                    (.stopPropagation e)
                    (.preventDefault e))
          on-mouse-up (fn on-mouse-up [e]
                        (events/unlisten js/document EventType.MOUSEMOVE on-move)
                        (events/unlisten js/document EventType.TOUCHMOVE on-move)
                        (events/unlisten js/document EventType.MOUSEUP on-mouse-up)
                        (events/unlisten js/document EventType.TOUCHEND on-mouse-up)
                        (if on-drag-end
                          (dispatch on-drag-end)))]
      (events/listen js/document EventType.MOUSEMOVE on-move)
      (events/listen js/document EventType.TOUCHMOVE on-move #js {:passive false})
      (events/listen js/document EventType.MOUSEUP on-mouse-up)
      (events/listen js/document EventType.TOUCHEND on-mouse-up)
      (if on-drag-start
        (dispatch (conj on-drag-start start))))))
