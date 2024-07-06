(ns magritte.statements.let
  (:require
   [magritte.statements.common :refer [replace-symbol]]
   [magritte.statements.format :refer [format-statement]]
   [magritte.utils :as utils]))

(defn format-let
  "Format let statement."
  [statement]
  (let [[_ [item value] block] statement
        block (if block (-> block
                            (replace-symbol item)
                            (format-statement))
                  nil)]
    (str "LET $" item " = " (utils/->query-str value) ";" (if block (str "\n" block) ""))))
