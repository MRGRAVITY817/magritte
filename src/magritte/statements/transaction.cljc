(ns magritte.statements.transaction)

(defn format-begin
  [expr]
  (when (or (= expr '(begin-transaction))
            (= expr '(begin)))
    "BEGIN TRANSACTION;"))

(defn format-cancel
  [expr]
  (when (or (= expr '(cancel-transaction))
            (= expr '(cancel)))
    "CANCEL TRANSACTION;"))

(defn format-commit
  [expr]
  (when (or (= expr '(commit-transaction))
            (= expr '(commit)))
    "COMMIT TRANSACTION;"))
