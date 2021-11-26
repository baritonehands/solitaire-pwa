(ns solitaire.build
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]))

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

(defn delete-directory-recursive
  "Recursively delete a directory."
  [^java.io.File file]
  ;; when `file` is a directory, list its entries and call this
  ;; function with each entry. can't `recur` here as it's not a tail
  ;; position, sadly. could cause a stack overflow for many entries?
  ;; thanks to @nikolavojicic for the idea to use `run!` instead of
  ;; `doseq` :)
  (when (.isDirectory file)
    (doseq [dir-file (.listFiles file)]
      (delete-directory-recursive dir-file)))
  ;; delete the file or directory. if it it's a file, it's easily
  ;; deletable. if it's a directory, we already have deleted all its
  ;; contents with the code above (remember?)
  (io/delete-file file))

(defn clean []
  (delete-directory-recursive (io/file "pwa" "assets" "build")))

(defn print-sh [& args]
  (let [{:keys [out err]} (apply sh args)]
    (if out
      (println out))
    (if err
      (.println *err* err))))

(defn gh-pages [out-path]
  (print-sh "cp" "-R" (.getAbsolutePath (io/file "pwa/*")) (.getAbsolutePath (io/file out-path)))
  (print-sh "sed" "-i" "''" "'s:href=\"/\":href=\"/solitaire/\":g'" (.getAbsolutePath (io/file out-path "index.html"))))