(ns magritte.statements.common
  (:require [magritte.utils :as utils]))

(defn handle-timeout [timeout]
  (when timeout
    (let [time (cond
                 (number? timeout) (str timeout "s")
                 :else (name timeout))]
      (str "TIMEOUT " time))))

(defn handle-parallel [parallel]
  (if parallel "PARALLEL" nil))

(defn handle-where [where]
  (when where
    (str "WHERE "
         (cond
           (list? where) (utils/list->str where)
           :else (name where)))))
