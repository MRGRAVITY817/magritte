(ns magritte.statements.let
  (:require
   [magritte.utils :as utils]))

(defn format-let
  "Format let statement."
  [statement]
  (let [[_ [item value]] statement]
    (str "LET $" item " = " (utils/->query-str value) ";")))
