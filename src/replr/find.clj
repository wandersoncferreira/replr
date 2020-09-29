(ns replr.find
  (:require [orchard.meta :as m]
            [orchard.query :as query]
            [orchard.xref :as xref]))

(def uninteresting-namespaces
  ["cider"
   "nrepl"
   "refactor-nrepl"])

(def var->name (fn [v] (let [m (meta v)] (str (:ns m) "/" (:name m)))))

(defn find-all-vars-current-project []
  (let [nss (query/namespaces {:load-project-ns? true
                               :project? true})]
    (->> nss
         (map str)
         (map re-pattern)
         (map #(query/vars {:search %}))
         flatten
         (map #(hash-map (var->name %) %))
         (into {}))))

(defn find-all-vars []
  (let [all-vars (query/vars {:private? true})]
    (->> all-vars
         (map #(hash-map (var->name %) %))
         (remove #(some true? (map (fn [v]
                                     (.startsWith (first (keys %)) v)) uninteresting-namespaces)))
         (into {}))))

(defn find-fn-dependencies [vr]
  (->> vr
       xref/fn-deps
       (map var->name)))

(defn find-fn-references [name]
  (let [sym (symbol name)]
    (->> sym
         xref/fn-refs
         (map var->name))))

(defn find-source-code [var]
  (try
    (or (:code (m/var-code var))
        (m/var-doc var))
    (catch Throwable t
      "Could not find source code or documentation.")))
