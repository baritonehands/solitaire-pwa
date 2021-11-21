(ns solitaire.drag
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-com.core :refer [box title]]
            [re-frame.core :refer [dispatch subscribe]]))

(defn mouse-down-handler
  ([pos event] (mouse-down-handler pos event {:invert? false
                                              :scale   1}))
  ([[x y] event opts]
   (fn [e]
     (dispatch [:drag/start {:offset [(.-clientX e)
                                      (.-clientY e)]
                             :start  [x y]
                             :opts   opts
                             :event  event}])
     (.stopPropagation e)
     (.preventDefault e)
     e)))

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


