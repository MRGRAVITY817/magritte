(ns magritte.statements.let
  (:require
   [magritte.statements.common :refer [replace-symbol]]
   [magritte.statements.format :refer [format-statement]]))

(defn format-let
  "Format let statement."
  [statement]
  (let [[_ [item value] block] statement
        value (format-statement value {:add-semicolon?        true
                                       :surround-with-parens? true})
        block (if block (-> block
                            (replace-symbol item)
                            (format-statement {:add-semicolon?        true
                                               :surround-with-parens? false}))
                  nil)]
    (str "LET $" item " = " value (if block (str "\n" block) ""))))
