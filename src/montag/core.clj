(ns montag.core
  (:require [cljfx.api :as fx]
            [clojure.string :as cstr]
            [montag.render :as render]
            [montag.find :as mfind]
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

;; (m/var-code (last (query/vars {:private? true})))

(defmacro if-lety
  ([bindings then]
   `(if-lety ~bindings ~then nil))
  ([bindings then else & _]
   (let [form (bindings 0) tst (bindings 1)]
     `(let [temp# ~tst]
        (if (and temp# (not-empty temp#))
          (let [~form temp#]
            ~then)
          ~else)))))

(defn root [arg]
  {:fx/type :stage
   :showing true
   :title "Montag"
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type :h-box
                              :spacing 250
                              :padding 5
                              :alignment :center-left
                              :children [{:fx/type :text
                                          :text "All Symbols"}
                                         {:fx/type :text
                                          :text "Dependencies"}
                                         {:fx/type :text
                                          :text "References"}]}
                             {:fx/type :h-box
                              :padding 5
                              :fill-height false
                              :spacing 20
                              :children [{:fx/type :list-view
                                          :min-width 300
                                          :on-selected-item-changed #(let [vr (get-in @*state [:all-fns %])]
                                                                       (swap! *state assoc :fn-inside (mfind/find-fn-dependencies vr))
                                                                       (swap! *state assoc :fn-references (mfind/find-fn-references %))
                                                                       (swap! *state assoc :fn-clicked %))
                                          :items (if-lety [filtered-fns (:filter-fns @*state)]
                                                   filtered-fns
                                                   (do
                                                     (swap! *state assoc :filter-fns (sort (keys (:all-fns @*state))))
                                                     (:filter-fns @*state)))}
                                         
                                         {:fx/type :list-view
                                          :min-width 300
                                          :on-selected-item-changed #(do
                                                                       (swap! *state assoc :fn-references (mfind/find-fn-references %))
                                                                       (swap! *state assoc :fn-clicked %))
                                          :items (:fn-inside @*state)}
                                         
                                         {:fx/type :list-view
                                          :min-width 300
                                          :on-selected-item-changed #(swap! *state assoc :fn-clicked %)
                                          :items (:fn-references @*state)}]}
                             {:fx/type :h-box
                              :padding 5
                              :spacing 20
                              :children [{:fx/type :button
                                          :text "All ns"
                                          :on-action (fn [_] (swap! *state assoc :filter-fns (sort (keys (:all-fns @*state)))))}
                                         {:fx/type :button
                                          :text "Project ns"
                                          :on-action (fn [_]
                                                       (let [prj-namespaces (map str (nas/project-namespaces))
                                                             filtered (filter (fn [s]
                                                                                (some true? (map #(.startsWith s %)
                                                                                                 prj-namespaces)))
                                                                              (keys (:all-fns @*state)))]
                                                         (swap! *state assoc :filter-fns filtered)))}
                                         {:fx/type :button
                                          :text "Current ns"
                                          :on-action {:event/type ::filter-current-ns}}]}
                             {:fx/type :v-box
                              :padding 5
                              :spacing 20
                              :children [(if-let [vr-clicked (get-in @*state [:all-fns (:fn-clicked @*state)])]
                                           (->> vr-clicked
                                                mfind/find-source-code
                                                render/generate-html
                                                render/display-html)
                                           (render/display-html "Read more code"))]}]}}})

(defn map-event-handler [event]
  (case (:event/type event)
    ::filter-current-ns (filter-namespace (:fn-clicked @*state))))

(defn start []
  (fx/mount-renderer
   *state
   (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler map-event-handler})))

(comment
  
  (start)

  )
