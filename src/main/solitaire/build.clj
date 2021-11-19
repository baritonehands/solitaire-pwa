(ns solitaire.build
  (:require [clojure.java.io :as io]))

(defn copy-resource [filename]
  (let [in-path (str "public/assets/" filename)
        out-path (str "pwa/assets/re-com/" filename)]
    (with-open [in (io/input-stream (io/resource in-path))]
      (io/make-parents out-path)
      (io/copy in (io/file out-path)))))

(defn copy-all-resources []
  (doseq [filename ["css/bootstrap.css"
                    "css/material-design-iconic-font.min.css"
                    "css/re-com.css"
                    "css/chosen-sprite.png"
                    "css/chosen-sprite@2x.png"
                    "fonts/Material-Design-Iconic-Font.eot"
                    "fonts/Material-Design-Iconic-Font.svg"
                    "fonts/Material-Design-Iconic-Font.ttf"
                    "fonts/Material-Design-Iconic-Font.woff"
                    "fonts/Material-Design-Iconic-Font.woff2"
                    "scripts/detect-element-resize.js"]]
    (copy-resource filename)))
