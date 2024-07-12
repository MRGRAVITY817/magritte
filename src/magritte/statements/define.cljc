(ns magritte.statements.define)

(defn format-define-database
  "Formats a define database expression."
  [expr]
  (when (and (list? expr)
             (= (first expr) 'defdb)
             (keyword? (second expr)))
    (str "DEFINE DATABASE " (name (second expr)) ";")))
