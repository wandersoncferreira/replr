(ns montag.filter
  (:require [clojure.string :as cstr]))

(defn filter-namespace [name all-fns]
  (let [[nms _] (cstr/split name #"/")]
    (filter #(.startsWith % nms) (keys all-fns))))
