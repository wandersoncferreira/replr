(ns replr.ui
  (:require [cljfx.api :as fx]
            [replr.events :as -events]
            [replr.state :as -state]
            [replr.utils.views :as -views]))

(defn root [{:keys [all-loaded-vars
                    all-loaded-ns
                    selected-fn-dependencies
                    selected-fn-references
                    source-code]}]
  {:fx/type :stage
   :showing true
   :title "Replr: Read Eval Print Loop, and READ"
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type :h-box
                              :spacing 250
                              :padding 5
                              :alignment :center-left
                              :children [{:fx/type :text :text (format "Namespaces - %s" (count all-loaded-ns))}
                                         {:fx/type :text :text (format "All Symbols - %s" (count all-loaded-vars))}
                                         {:fx/type :text :text (format "Dependencies - %s" (count selected-fn-dependencies))}
                                         {:fx/type :text :text (format "References - %s" (count selected-fn-references))}]}
                             {:fx/type :h-box
                              :padding 5
                              :spacing 20
                              :children [{:fx/type -views/list-view
                                          :items (sort all-loaded-ns)
                                          :panel :all-loaded-ns}
                                         {:fx/type -views/list-view
                                          :items (sort (keys all-loaded-vars))
                                          :panel :all-loaded-vars}
                                         {:fx/type -views/list-view
                                          :items selected-fn-dependencies
                                          :panel :selected-fn-dependencies}
                                         {:fx/type -views/list-view
                                          :items selected-fn-references
                                          :panel :selected-fn-references}
                                         ]}
                             {:fx/type :h-box
                              :padding 5
                              :spacing 20
                              :children [{:fx/type -views/button-view
                                          :text "All ns"
                                          :action :show-all-namespaces}
                                         {:fx/type -views/button-view
                                          :text "Project ns"
                                          :action :show-project-namespaces}
                                         ]}
                             {:fx/type :v-box
                              :padding 5
                              :spacing 20
                              :children [{:fx/type -views/display-html
                                          :code source-code}]}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler -events/handler}))

(defn open []
  (fx/mount-renderer
   -state/db
   renderer
   ))

(defn -main [& args]
  (open))
