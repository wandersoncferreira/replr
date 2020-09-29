(ns montag.core
  (:require [montag.find :as mfind]
            [montag.render :as render]
            [montag.state :as -state]
            [montag.utils.macro :as macro]))

(defmulti list-view-on-item-change (fn [panel] panel))

(defmethod list-view-on-item-change :all-fns
  [_]
  (fn [value-clicked]
    (let [vr (get-in @-state/db [:all-fns value-clicked])]
      (swap! -state/db merge {:fn-inside (mfind/find-fn-dependencies vr)
                              :fn-references (mfind/find-fn-references value-clicked)
                              :fn-clicked value-clicked}))))

(defmethod list-view-on-item-change :dependencies
  [_]
  (fn [value-clicked]
    (swap! -state/db merge {:fn-references (mfind/find-fn-references value-clicked)
                            :fn-clicked value-clicked})))

(defmethod list-view-on-item-change :references
  [_]
  (fn [value-clicked]
    (swap! -state/db assoc :fn-clicked value-clicked)))

(defmulti items (fn [panel] panel))

(defmethod items :all-fns
  [_]
  (macro/if-lety [filtered-fns (:filter-fns @-state/db)]
                 (sort (set filtered-fns))
                 (do
                   (swap! -state/db assoc :filter-fns (keys (:all-fns @-state/db)))
                   (sort (set (:filter-fns @-state/db))))))

(defmethod items :dependencies
  [_]
  (set (:fn-inside @-state/db)))

(defmethod items :references
  [_]
  (set (:fn-references @-state/db)))

(defn show-source-code []
  (if-let [vr-clicked (get-in @-state/db [:all-fns (:fn-clicked @-state/db)])]
    (->> vr-clicked
         mfind/find-source-code
         render/generate-html
         render/display-html)
    (render/display-html "Read more code")))

