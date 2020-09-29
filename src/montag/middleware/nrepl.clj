(ns montag.middleware.nrepl
  (:require [nrepl.transport :as transport]
            [nrepl.middleware.print :refer [wrap-print]]
            [nrepl.middleware :refer [set-descriptor!]]
            [montag.state :as state])
  (:import [nrepl.transport Transport]))

(defn send-to-montag!
  [{:keys [code]} {:keys [value] :as resp}]
  (when (and code (contains? resp :value) (var? value))
    (state/update-vars! value)))

(defn- wrap-montag-sender
  [{:keys [^Transport transport] :as request}]
  (reify transport/Transport
    (recv [this]
      (.recv transport))
    (recv [this timeout]
      (.recv transport timeout))
    (send [this resp]
      (.send transport (send-to-montag! request resp))
      this)))

(defn wrap-montag [handler]
  (fn [request]
    (handler (assoc request :transport (wrap-montag-sender request)))))

(set-descriptor! #'wrap-montag
                 {:requires #{#'wrap-print}
                  :expects #{"eval"}})
