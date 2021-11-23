(ns solitaire.drag
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-com.core :refer [box title]]
            [re-frame.core :refer [dispatch subscribe]]))

(defn mouse-down-handler
  ([*elem event] (mouse-down-handler event {:invert? false
                                            :scale   1}))
  ([*elem event opts]
   (fn [e]
     (when (and
             (= (.-target e) @*elem)
             (or (= (.-type e) "touchstart")
                 (zero? (.-button e))))
       (let [dims (case (.-type e)
                    "touchstart" (aget e "targetTouches" 0)
                    e)
             {:keys [x y]} (-> (.getBoundingClientRect @*elem)
                               (.toJSON)
                               (js->clj :keywordize-keys true))]
         (println "dispatching start" [x y] [(.-clientX dims)
                                             (.-clientY dims)])
         (dispatch [:drag/start {:offset [(.-clientX dims)
                                          (.-clientY dims)]
                                 :start  [x y]
                                 :opts   opts
                                 :event  event}])
         e)))))

(defn target
  "Automatically wrap component as drag target (must pass box args)"
  [path & {:keys [child] :as args}]
  (r/with-let [*elem (r/atom nil)
               *hover (subscribe [:deck/droppable])
               handler (fn []
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
                                           (handler)))}
                            child]]))
        (vec))
    (finally
      (.removeEventListener js/window "resize" handler)
      (dispatch [:drag/remove-target path]))))


