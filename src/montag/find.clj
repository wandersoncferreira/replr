(ns montag.find
  (:require [orchard.query :as query]
            [orchard.meta :as m]
            [orchard.xref :as xref]))

(def uninteresting-namespaces
  ["cider"
   "nrepl"
   "refactor-nrepl"])

(defn find-all-vars []
  (let [all-vars (query/vars {:private? true})]
    (->> all-vars
         (pmap #(hash-map (str (:ns (meta %)) "/" (:name (meta %))) %))
         (remove #(some true? (map (fn [v]
                                     (.startsWith (first (keys %)) v)) uninteresting-namespaces)))
         (into {}))))

(defn find-fn-dependencies [vr]
  (let [fmt (fn [vr]
              (let [mt (meta vr)]
                (str (:ns mt) "/" (:name mt))))]
    (->> vr
         xref/fn-deps
         (map fmt))))

(defn find-fn-references [name]
  (let [sym (symbol name)
        fmt (fn [vr]
              (let [mt (meta vr)]
                (str (:ns mt) "/" (:name mt))))]
    (->> sym
         xref/fn-refs
         (map fmt))))

(defn find-source-code [var]
  (try
    (or (:code (m/var-code var))
        (m/var-doc var))
    (catch Throwable t
      "Could not find source code or documentation.")))
