(ns solitaire.workbox.prompt
  (:require [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :refer [modal-panel box  h-box v-box p title button]]))

(defn prompt-button [& {:as args}]
  (->> args
       (mapcat identity)
       (into [button :parts {:wrapper {:style {:flex "1 0 auto"}}}])))

(defn view []
  (let [show-prompt? @(subscribe [:workbox/show-prompt?])]
    (if show-prompt?
      [modal-panel
       :child
       [v-box
        :gap "5px"
        :children
        [[title :level :level2 :label "Update Available"]
         [p "A new version of Solitaire is available. Click \"Update and Reload\" to update to the latest version."]
         [h-box
          :children
          [[box
            :size "1"
            :child
            [prompt-button
             :label "Update and Reload"
             :class "btn-primary btn-lg btn-block"
             :on-click #(dispatch [:workbox/update])]]
           [box
            :size "1"
            :child
            [prompt-button
             :label "Ignore"
             :class "btn-lg btn-block"
             :on-click #(dispatch [:workbox/show-prompt false])]]]]]]])))
