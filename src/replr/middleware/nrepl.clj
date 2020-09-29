(ns replr.middleware.nrepl
  (:require [replr.state :as -state]
            [nrepl.middleware :refer [set-descriptor!]]
            [nrepl.middleware.print :refer [wrap-print]]
            [nrepl.transport :as transport])
  (:import nrepl.transport.Transport))

(defn send-to-replr!
  [{:keys [code]} {:keys [value] :as resp}]
  (when (and code (contains? resp :value) (var? value))
    (-state/update-vars! value))
  resp)

(defn- wrap-replr-sender
  [{:keys [id op ^Transport transport] :as request}]
  (reify transport/Transport
    (recv [this]
      (.recv transport))
    (recv [this timeout]
      (.recv transport timeout))
    (send [this resp]
      (.send transport (send-to-replr! request resp))
      this)))

(defn wrap-replr [handler]
  (fn [{:keys [id op transport] :as request}]
    (handler (assoc request :transport (wrap-replr-sender request)))))

(set-descriptor! #'wrap-replr
                 {:requires #{#'wrap-print}
                  :expects #{"eval"}})
