(ns montag.core
  (:require [cljfx.api :as fx]
            [clojure.string :as cstr]
            [montag.render :as render]
            [montag.find :as mfind]
            [montag.utils.macro :as macro]
            [montag.utils.views :as -views]
            [montag.filter :as -filter]
            [orchard.meta :as m]
            [orchard.namespace :as nas]
            [orchard.query :as query]
            [orchard.xref :as xref]))

(def uninteresting-namespaces
  ["cider"
   "nrepl"
   "refactor-nrepl"])

(def *state (atom {:fn-inside []
                   :fn-references []
                   :fn-clicked nil
                   :all-fns (mfind/find-all-vars)
                   :filter-fns []}))

(defn filter-namespace [name]
  (let [[nms _] (cstr/split name #"/")]
    (swap! *state assoc :filter-fns (filter #(.startsWith % nms) (keys (:all-fns @*state))))))

(defmulti list-view-on-item-change (fn [panel] panel))

(defmethod list-view-on-item-change :all-fns
  [_]
  (fn [value-clicked]
    (let [vr (get-in @*state [:all-fns value-clicked])]
      (swap! *state assoc :fn-inside (mfind/find-fn-dependencies vr))
      (swap! *state assoc :fn-references (mfind/find-fn-references value-clicked))
      (swap! *state assoc :fn-clicked value-clicked))))

(defmethod list-view-on-item-change :dependencies
  [_]
  (fn [value-clicked]
    (swap! *state assoc :fn-references (mfind/find-fn-references value-clicked))
    (swap! *state assoc :fn-clicked value-clicked)))

(defmethod list-view-on-item-change :references
  [_]
  (fn [value-clicked]
    (swap! *state assoc :fn-clicked value-clicked)))

(defmulti items (fn [panel] panel))

(defmethod items :all-fns
  [_]
  (macro/if-lety [filtered-fns (:filter-fns @*state)]
                 filtered-fns
                 (do
                   (swap! *state assoc :filter-fns (sort (keys (:all-fns @*state))))
                   (:filter-fns @*state))))

(defmethod items :dependencies
  [_]
  (:fn-inside @*state))

(defmethod items :references
  [_]
  (:fn-references @*state))


(defn show-source-code []
  (if-let [vr-clicked (get-in @*state [:all-fns (:fn-clicked @*state)])]
    (->> vr-clicked
         mfind/find-source-code
         render/generate-html
         render/display-html)
    (render/display-html "Read more code")))

(defn root [_]
  {:fx/type :stage
   :showing true
   :title "Montag"
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type :h-box
                              :spacing 250
                              :padding 5
                              :alignment :center-left
                              :children [{:fx/type :text :text "All Symbols"}
                                         {:fx/type :text :text "Dependencies"}
                                         {:fx/type :text :text "References"}]}
                             {:fx/type :h-box
                              :padding 5
                              :fill-height false
                              :spacing 20
                              :children [(-views/list-view (list-view-on-item-change :all-fns) (items :all-fns))
                                         (-views/list-view (list-view-on-item-change :dependencies) (items :dependencies))
                                         (-views/list-view (list-view-on-item-change :references) (items :references))]}
                             {:fx/type :h-box
                              :padding 5
                              :spacing 20
                              :children [{:fx/type :button
                                          :text "All ns"
                                          :on-action {:event/type ::remove-all-filters}}
                                         {:fx/type :button
                                          :text "Project ns"
                                          :on-action {:event/type ::filter-project-ns}}
                                         {:fx/type :button
                                          :text "Current ns"
                                          :on-action {:event/type ::filter-current-ns}}]}
                             {:fx/type :v-box
                              :padding 5
                              :spacing 20
                              :children [show-source-code]}]}}})

(defn map-event-handler [event]
  (case (:event/type event)
    ::remove-all-filters (swap! *state assoc :filter-fns (sort (keys (:all-fns @*state))))
    ::filter-current-ns (filter-namespace (:fn-clicked @*state))
    ::filter-project-ns (let [prj-namespaces (map str (nas/project-namespaces))
                              filtered (filter (fn [s]
                                                 (some true? (map #(.startsWith s %)
                                                                  prj-namespaces)))
                                               (keys (:all-fns @*state)))]
                          (swap! *state assoc :filter-fns filtered))))

(defn start []
  (fx/mount-renderer
   *state
   (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler map-event-handler})))

(comment
  
  (start)

  )
