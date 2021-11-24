(ns solitaire.drag
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-com.core :refer [box title]]
            [re-frame.core :refer [dispatch subscribe]]
            [goog.object :as gobj]))

(def double-click-duration 500)

(defn mouse-down-handler
  ([*elem event] (mouse-down-handler event {:invert? false
                                            :scale   1}))
  ([*elem event opts]
   (let [clicks #js {:mousedown 0
                     :touchstart 0}
         timers #js {:mousedown nil
                     :touchstart nil}]
     (fn [e]
       (let [event-type (.-type e)]
         (when (and
                 (= (.-target e) @*elem)
                 (or (= event-type "touchstart")
                     (zero? (.-button e))))
           ;(println "handle" event-type "\n\n" (.-outerHTML @*elem))
           (gobj/set clicks event-type (inc (gobj/get clicks event-type)))
           (js/console.dir clicks @*elem)
           (set! js/window.debugDrag clicks)
           (case (gobj/get clicks event-type)
             1 (gobj/set timers event-type (js/setTimeout (fn []
                                                            (println "Single" event-type)
                                                            (gobj/set clicks event-type 0))
                                                          double-click-duration))
             2 (do
                 (println "Double" event-type)
                 (js/clearTimeout (gobj/get timers event-type))
                 (gobj/set clicks event-type 0)
                 (gobj/set clicks event-type nil)
                 (if (:on-double-click opts)
                   (dispatch (:on-double-click opts)))))
           (let [dims (case event-type
                        "touchstart" (aget e "touches" 0)
                        e)
                 {:keys [x y]} (-> (.getBoundingClientRect @*elem)
                                   (.toJSON)
                                   (js->clj :keywordize-keys true))]
             (dispatch [:drag/start {:offset [(.-clientX dims)
                                              (.-clientY dims)]
                                     :start  [(+ x js/window.scrollX)
                                              (+ y js/window.scrollY)]
                                     :opts   opts
                                     :event  event}]))))))))

(defn target
  "Automatically wrap component as drag target (must pass box args)"
  [path & {:keys [child] :as args}]
  (r/with-let [*elem (atom nil)
               *hover (subscribe [:deck/droppable])
               handler (fn [e]
                         (let [bounds (-> (.getBoundingClientRect @*elem)
                                          (.toJSON)
                                          (js->clj :keywordize-keys true))]
                           (dispatch [:drag/set-target path bounds])))
               _ (.addEventListener js/window "resize" handler)]
              (-> args
                  (cond-> (= @*hover path) (assoc :class "droppable"))
                  (dissoc :child)
                  (->> (reduce
                         concat
                         [box :child [:div {:ref (fn [elem]
                                                   (when elem
                                                     (reset! *elem elem)
                                                     (handler #js {})))}
                                      child]]))
                  (vec))
              (finally
                (.removeEventListener js/window "resize" handler)
                (dispatch [:drag/remove-target path]))))


