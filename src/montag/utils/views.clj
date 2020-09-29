(ns montag.utils.views)

(def ^:dynamic *list-view-min-width* 300)

(defn list-view [f items]
  {:fx/type :list-view
   :min-width *list-view-min-width*
   :on-selected-item-changed f
   :items items 
   })
