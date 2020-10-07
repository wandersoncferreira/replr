(ns replr.state
  (:require [replr.find :as -find]))

(defonce ^:private all-vars (-find/find-all-vars))

(defonce ^:private all-ns (-find/find-all-namespaces))

(def db (atom {:source-code "Read more source code!"
               :all-loaded-vars all-vars
               :all-loaded-ns all-ns
               :selected-fn-dependencies (list)
               :selected-fn-references (list)
               :selections (list)
               :selection ""
               }))

(defn update-vars! [var]
  (let [name (str (:ns (meta var)) "/" (:name (meta var)))]
    (swap! db update :filter-fns conj name)
    (swap! db assoc-in [:all-fns name] var)))

