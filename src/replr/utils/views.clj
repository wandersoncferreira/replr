(ns replr.utils.views
  (:require [cljfx.api :as fx]
            [cljfx.prop :as fx.prop]
            [cljfx.mutator :as fx.mutator]
            [cljfx.ext.list-view :as fx.ext.list-view]
            [cljfx.lifecycle :as fx.lifecycle]
            [clojure.string :as cstr]
            [clojure.java.io :as io]))

(def ^:dynamic *list-view-min-width* 300)

(defn list-view [{:keys [items panel selection selection-mode]}]
  {:fx/type fx.ext.list-view/with-selection-props
   :props (case selection-mode
            :multiple {:selection-mode :multiple
                       :selected-items selection
                       :on-selected-items-changed {:event/type (keyword (str "multiple-selection/" (name panel)))}}
            :single (cond-> {:selection-mode :single
                             :on-selected-item-changed {:event/type (keyword (str "single-selection/" (name panel)))}}
                      (seq selection)
                      (assoc :selected-item (-> selection sort first))))
   :desc {:fx/type :list-view
          :cell-factory {:fx/cell-type :list-cell
                         :describe (fn [path] {:text path})}
          :min-width *list-view-min-width*
          :items items}})

(defn button-view [{:keys [text action]}]
  {:fx/type :button
   :text text
   :on-action {:event/type (keyword (str "filter/" (name action)))}})

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

(defn display-html [{:keys [code]}]
  (let [code (or code "Read more source code!")]
    {:fx/type ext-with-html
     :props {:html (generate-html code)}
     :desc {:fx/type :web-view
            :font-scale 1.1
            :context-menu-enabled false
            :max-width 940}}))
