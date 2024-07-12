(ns magritte.statements.define)

(defn format-define-database
  "Formats a define database expression."
  [[fn-name db-name extra :as expr]]
  (when (and (list? expr)
             (= fn-name 'defdb)
             (keyword? db-name))
    (let [if-not-exists (if (= extra :if-not-exists)
                          "IF NOT EXISTS "
                          "")
          db-name (name db-name)]
      (str "DEFINE DATABASE "
           if-not-exists
           db-name
           ";"))))
