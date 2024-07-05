(ns magritte.statements.for
  (:require
   [clojure.string :as str]
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
          block (last for-statement)
          iterable (cond
                     (vector? iterable) (utils/->query-str iterable)
                     :else (str "(" (str/replace (format-statement iterable) #";" "") ")"))]
      (str "FOR $" item " IN " iterable " { " (format-statement block) " };"))))

"FOR $person IN (SELECT VALUE id FROM person WHERE age >= 18) { UPDATE $person SET can_vote = true; };"

"FOR $person IN (SELECT VALUE id FROM person WHERE (age >= 18);) { UPDATE $person SET [can_vote, true]; };"
