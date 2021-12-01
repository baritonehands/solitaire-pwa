(ns solitaire.dialog.confirm
  (:require [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :refer [modal-panel box h-box v-box p title button]]))

(defn prompt-button [& {:as args}]
  (->> args
       (mapcat identity)
       (into [button :parts {:wrapper {:style {:flex "1 0 auto"}}}])))

(defn view []
  (if-let [{:keys      [text confirm-label cancel-label on-confirm]
            title-text :title} @(subscribe [:dialog/confirm])]
    [modal-panel
     :backdrop-on-click #(dispatch [:dialog/confirm-cancel])
     :style {:max-width "100vw"}
     :child
     [v-box
      :gap "5px"
      :children
      [[title :level :level2 :label title-text]
       [p {:style {:width     "95%"
                   :min-width "325px"}} text]
       [h-box
        :children
        [[box
          :size "1"
          :child
          [prompt-button
           :label confirm-label
           :class "btn-primary btn-lg btn-block"
           :on-click (if (fn? on-confirm)
                       on-confirm
                       #(dispatch on-confirm))]]
         [box
          :size "1"
          :child
          [prompt-button
           :label cancel-label
           :class "btn-lg btn-block"
           :on-click #(dispatch [:dialog/confirm-cancel])]]]]]]]))
