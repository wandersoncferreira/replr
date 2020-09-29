(ns montag.filter
  (:require [clojure.string :as cstr]))

(defn filter-namespace [state name]
  (let [[nms _] (cstr/split name #"/")]
    (assoc state :filter-fns (filter #(.startsWith % nms)
                                     (keys (:all-fns state))))))
