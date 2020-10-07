(ns replr.graph
  (:require [clojure.java.io :as io]
            [replr.state :as -state]
            [rhizome.viz :as viz]))

;;; TODO: huuugeee refactor needed! in fact, I might try the database approach instead of a regular atom.
;;; nice to practice datomic
;;; FIXME: there are some bugs... ofc.. impossible to tell from the current impl.
(defn get-graph []
  (let [all-vars (:selections @-state/db)
        all-dependents (:selected-fn-dependencies @-state/db)
        all-references (:selected-fn-references @-state/db)
        graph (->> all-vars
                 (reduce
                  (fn [acc v]
                    (assoc acc v (keys all-dependents)))
                  {})
                 (#(reduce
                      (fn [acc v]
                        (assoc acc v (list)))
                      %
                      (keys all-dependents)))
                 (#(reduce
                    (fn [acc v]
                      (let [c (get acc v)
                            refs (if (empty? c)
                                    (keys all-references)
                                    (concat c (keys all-references)))]
                        (reduce
                         (fn [acc r]
                           (let [cc (get acc r)
                                 deps (if (empty? cc)
                                        (list)
                                        cc)]
                             (assoc acc r deps)))
                         (assoc acc v refs)
                         (keys all-references))))
                    %
                    all-vars)))
        tmp-file (java.io.File/createTempFile "graph" ".svg")]
    (with-open [file (io/writer tmp-file)]
      (binding [*out* file]
        (println (viz/graph->svg (keys graph) graph
                                 :directed? true
                                 :vertical? false
                                 :node->descriptor (fn [x] {:label x :tooltip "VAR DOCS!"})
                                 :options {:size "7,7!"}))))
    (str "file:" (.getAbsolutePath tmp-file))))
