(ns replr.events
  (:require [replr.find :as -find]
            [replr.state :as -state]))

(defmulti handler (fn [event] (:event/type event)))

(defmethod handler :filter/show-all-namespaces
  [_]
  (swap! -state/db assoc :all-loaded-vars (-find/find-all-vars))
  (swap! -state/db assoc :all-loaded-ns (-find/find-all-namespaces))
  (swap! -state/db assoc :selected-fn-dependencies (list))
  (swap! -state/db assoc :selected-fn-references (list)))

(defmethod handler :filter/show-project-namespaces
  [_]
  (let [vars (-find/find-all-vars-current-project)
        ns-project (-find/find-project-namespaces)]
    (swap! -state/db assoc :all-loaded-vars vars)
    (swap! -state/db assoc :all-loaded-ns ns-project)
    (swap! -state/db assoc :selected-fn-dependencies (list))
    (swap! -state/db assoc :selected-fn-references (list))))

(defmethod handler :multiple-selection/all-loaded-ns
  [event]
  (let [vars (-find/find-vars-from-namespaces (:fx/event event))]
    (swap! -state/db assoc :all-loaded-vars vars)))

(defmethod handler :multiple-selection/all-loaded-vars
  [event]
  (let [selected-fn-names (:fx/event event)
        selected-fn-vars (map #(get-in @-state/db [:all-loaded-vars %]) selected-fn-names)]

    (swap! -state/db assoc :selections selected-fn-names)

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
