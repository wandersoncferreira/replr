(ns replr.events
  (:require [replr.filter :as -filter]
            [replr.find :as -find]
            [replr.state :as -state]))

(defmulti handler (fn [event] (:event/type event)))

(defmethod handler :filter/remove-all-ns
  [_]
  (swap! -state/db assoc :filter-fns (sort (keys (:all-fns @-state/db))))
  (swap! -state/db assoc :all-ns (-find/find-all-namespaces)))

(defmethod handler :filter/project-ns
  [_]
  (let [vars (-find/find-all-vars-current-project)
        alls (:all-fns @-state/db)]
    (swap! -state/db assoc :filter-fns (keys vars))
    (swap! -state/db assoc :all-fns (merge vars alls))
    (swap! -state/db assoc :all-ns (-find/find-project-namespaces))))

(defmethod handler :filter/current-ns
  [_]
  (when-let [fn-name (:fn-clicked @-state/db)]
    (->> (:all-fns @-state/db)
         (-filter/filter-namespace fn-name )
         (swap! -state/db assoc :filter-fns))))


(defmethod handler :multiple-selection/all-loaded-ns
  [event]
  (let [vars (-find/find-vars-from-namespaces (:fx/event event))]
    (swap! -state/db assoc :all-loaded-vars vars)))

(defmethod handler :multiple-selection/all-loaded-vars
  [event]
  (let [selected-fn-names (:fx/event event)
        selected-fn-vars (map #(get-in @-state/db [:all-loaded-vars %]) selected-fn-names)]

    (when (= (count selected-fn-names) 1)
      (swap! -state/db assoc :source-code (-find/find-source-code (first selected-fn-vars))))

    (swap! -state/db assoc :selected-fn-dependencies
           (reduce
            (fn [acc v]
              (merge
               acc
               (-find/find-fn-dependencies v)))
            {}
            selected-fn-vars))))

(defmethod handler :multiple-selection/selected-fn-dependencies
  [event]
  (let [selected-fn-names (:fx/event event)
        selected-fn-vars (map #(get-in @-state/db [:selected-fn-dependencies %]) selected-fn-names)]

    (when (= (count selected-fn-names) 1)
      (swap! -state/db assoc :source-code (-find/find-source-code (first selected-fn-vars))))

    (swap! -state/db assoc :selected-fn-references
           (reduce
            (fn [acc v]
              (merge
               acc
               (-find/find-fn-references v)))
            {}
            selected-fn-vars))))

(defmethod handler :multiple-selection/selected-fn-references
  [event]
  (let [selected-fn-names (:fx/event event)
        selected-fn-vars (map #(get-in @-state/db [:selected-fn-references %]) selected-fn-names)]

    (when (= (count selected-fn-names) 1)
      (swap! -state/db assoc :source-code (-find/find-source-code (first selected-fn-vars))))))
