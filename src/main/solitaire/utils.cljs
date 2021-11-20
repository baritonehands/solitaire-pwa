(ns solitaire.utils
  (:require [goog.object :as gobj]))

(defn rect->clj [rect]
  (into {} (for [k (js-keys rect)]
             [(keyword k) (gobj/get rect k)])))
