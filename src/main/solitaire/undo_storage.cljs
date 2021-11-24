(ns solitaire.undo-storage
  (:require [re-frame.core :refer [->interceptor reg-cofx reg-fx]]
            [akiroz.re-frame.storage :as storage]
            [day8.re-frame.undo :as undo]))

(def undo-storage-key :undos)
(def redo-storage-key :redos)

(def undo-key :undo-storage)
(def redo-key :redo-storage)

(defn persist-undo []
  (->interceptor
    :id :undo-storage
    :before (fn [context]
              (-> context
                  (assoc-in [:coeffects undo-key] (storage/<-store undo-storage-key))
                  (assoc-in [:coeffects redo-key] (storage/<-store redo-storage-key))))
    :after (fn [context]
             (storage/->store undo-storage-key {:list         @undo/undo-list
                                                :explanations @undo/undo-explain-list})
             (storage/->store redo-storage-key {:list         @undo/redo-list
                                                :explanations @undo/redo-explain-list})
             context)))

(storage/register-store undo-storage-key)
(storage/register-store redo-storage-key)

(reg-cofx
  :undo-storage
  (fn [coeffects _]
    (assoc coeffects
      undo-key (storage/<-store undo-storage-key)
      redo-key (storage/<-store redo-storage-key))))

(reg-fx
  :undo/reset
  (fn [[undos redos]]
    (reset! undo/undo-list (:list undos))
    (reset! undo/undo-explain-list (:explanations undos))
    (reset! undo/redo-list (:list redos))
    (reset! undo/redo-explain-list (:explanations redos))))
