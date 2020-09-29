(ns replr.state
  (:require [replr.find :as -find]))

(defonce ^:private all-vars (-find/find-all-vars))

(def db (atom {:fn-inside []
               :fn-references []
               :fn-clicked nil
               :all-fns all-vars
               :filter-fns []}))

(defn update-vars! [var]
  (let [name (str (:ns (meta var)) "/" (:name (meta var)))]
    (swap! db update :filter-fns conj name)
    (swap! db assoc-in [:all-fns name] var)))

