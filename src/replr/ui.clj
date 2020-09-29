(ns replr.ui
  (:require [cljfx.api :as fx]
            [replr.core :as -core]
            [replr.events :as -events]
            [replr.state :as -state]
            [replr.utils.views :as -views])
  (:gen-class))

(defn root [_]
  {:fx/type :stage
   :showing true
   :title "Replr"
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
                              :children [(-views/list-view (-core/list-view-on-item-change :all-fns) (-core/items :all-fns))
                                         (-views/list-view (-core/list-view-on-item-change :dependencies) (-core/items :dependencies))
                                         (-views/list-view (-core/list-view-on-item-change :references) (-core/items :references))]}
                             {:fx/type :h-box
                              :padding 5
                              :spacing 20
                              :children [{:fx/type :button
                                          :text "All ns"
                                          :on-action {:event/type :filter/remove-all-ns}}
                                         {:fx/type :button
                                          :text "Project ns"
                                          :on-action {:event/type :filter/project-ns}}
                                         {:fx/type :button
                                          :text "Current ns"
                                          :on-action {:event/type :filter/current-ns}}]}
                             {:fx/type :v-box
                              :padding 5
                              :spacing 20
                              :children [(-core/show-source-code)]}]}}})


(defn open []
  (fx/mount-renderer
   -state/db
   (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler -events/handler})))

(defn -main [& args]
  (open))
