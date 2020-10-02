(ns replr.core
  (:require [replr.find :as mfind]
            [replr.render :as render]
            [replr.state :as -state]
            [replr.utils.macro :as macro]
            [orchard.query :as query]))

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

(def var->name (fn [v] (let [m (meta v)] (str (:ns m) "/" (:name m)))))

(defmethod list-view-on-item-change :namespaces
  [_]
  (fn [value-clicked]
    (println "VALUE CLICKED: " value-clicked)
    (println "SYMBOLS: " (query/vars {:search (re-pattern value-clicked)}))
    (swap! -state/db merge {:filter-fns (map var->name (query/vars {:search (re-pattern value-clicked)}))
                            :fn-clicked value-clicked})))

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

(defmethod items :namespaces
  [_]
  (set (:all-ns @-state/db)))

(defn show-source-code []
  (if-let [vr-clicked (get-in @-state/db [:all-fns (:fn-clicked @-state/db)])]
    (->> vr-clicked
         mfind/find-source-code
         render/generate-html
         render/display-html)
    (render/display-html "Read more code")))

