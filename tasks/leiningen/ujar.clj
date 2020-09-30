(ns leiningen.ujar
  (:require leiningen.uberjar
            leiningen.core.eval))

(defn ujar [project]
  (leiningen.uberjar/uberjar project)
  (leiningen.core.eval/eval-in-project project `(javafx.application.Platform/exit)))
