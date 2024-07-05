(ns magritte.statements.for
  (:require
   [magritte.statements.format :refer [format-statement]]
   [magritte.utils :as utils]))

(defn format-for
  "
  Format a for loop statement.

  ```
  FOR @item IN @iterable @block
  ```
  "
  [for-statement]
  (when (list? for-statement)
    (let [[item iterable] (second for-statement)
          block (last for-statement)]
      (prn item iterable block)
      (str "FOR $" item " IN " (utils/->query-str iterable) " { " (format-statement block) " };"))))
