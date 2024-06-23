(ns magritte.statements.common)

(defn handle-timeout [timeout]
  (when timeout
    (let [time (cond
                 (number? timeout) (str timeout "s")
                 :else (name timeout))]
      (str "TIMEOUT " time))))

(defn handle-parallel [parallel]
  (if parallel "PARALLEL" nil))
