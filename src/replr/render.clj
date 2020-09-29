(ns replr.render
  (:require [cljfx.api :as fx]
            [cljfx.prop :as fx.prop]
            [cljfx.mutator :as fx.mutator]
            [cljfx.lifecycle :as fx.lifecycle]
            [clojure.string :as cstr]
            [clojure.java.io :as io]))

(def ext-with-html
  "Custom prop to be able to load content in the Webview component."
  (fx/make-ext-with-props
   {:html (fx.prop/make
           (fx.mutator/setter #(.loadContent (.getEngine ^javafx.scene.web.WebView %1) %2))
           fx.lifecycle/scalar)}))

(defn generate-html
  "Create a HTML with the `code` embedded using Codemirror online editor
  to provide syntax highlight."
  [code]
  (-> (str "<!doctype html>" 
           "<html>" 
           "<head>" 
           "  <link rel=\"stylesheet\" href=\"codemirror.css\">" 
           "  <script src=\"codemirror.js\"></script>" 
           "  <script src=\"clojure.js\"></script>" 
           "</head>" 
           "<body>" 
           "<form><textarea id=\"code\" name=\"code\">\n" 
           code 
           "</textarea></form>" 
           "<script>" 
           "  var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {" 
           "    mode: \"clojure\"" 
           "  });" 
           "</script>" 
           "</body>" 
           "</html>")
      (cstr/replace #"codemirror.css" (.toExternalForm (io/resource "codemirror.css")))
      (cstr/replace #"codemirror.js" (.toExternalForm (io/resource "codemirror.js")))
      (cstr/replace #"clojure.js" (.toExternalForm (io/resource "clojure.js")))))


(defn display-html [code]
  {:fx/type ext-with-html
   :props {:html code}
   :desc {:fx/type :web-view
          :font-scale 1.1
          :max-width 940}})
