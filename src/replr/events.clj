(ns replr.events
  (:require [replr.filter :as -filter]
            [replr.find :as -find]
            [replr.state :as -state]))

(defmulti handler (fn [event] (:event/type event)))

(defmethod handler :filter/remove-all-ns
  [_]
  (swap! -state/db assoc :filter-fns (sort (keys (:all-fns @-state/db)))))

(defmethod handler :filter/project-ns
  [_]
  (let [vars (-find/find-all-vars-current-project)
        alls (:all-fns @-state/db)]
    (swap! -state/db assoc :filter-fns (keys vars))
    (swap! -state/db assoc :all-fns (merge vars alls))))

(defmethod handler :filter/current-ns
  [_]
  (when-let [fn-name (:fn-clicked @-state/db)]
    (->> (:all-fns @-state/db)
         (-filter/filter-namespace fn-name )
         (swap! -state/db assoc :filter-fns))))
