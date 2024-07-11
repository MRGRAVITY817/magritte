(ns magritte.statements.transaction)

(defn format-begin
  [expr]
  (when (or (= expr '(begin-transaction))
            (= expr '(begin)))
    "BEGIN TRANSACTION;"))
