(ns magritte.statements.for
  (:require
   [clojure.string :as str]
   [magritte.statements.common :refer [replace-symbol]]
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
          block (-> for-statement
                    (last)
                    (replace-symbol item))
          iterable (cond
                     (vector? iterable) (utils/->query-str iterable)
                     :else (str "(" (str/replace (format-statement iterable) #";" "") ")"))]
      (str "FOR $" item " IN " iterable " { " (format-statement block) " };"))))

"FOR $person IN (SELECT VALUE id FROM person WHERE age >= 18) { UPDATE $person SET can_vote = true; };"

"FOR $person IN (SELECT VALUE id FROM person WHERE (age >= 18)) { UPDATE $person SET [can_vote, true]; };"

(comment
  (def my-map '{:create  (type/thing "person" name)
                :content {:name name}})

  (-> (replace-symbol my-map 'name)
      (format-statement))

  (replace-symbol '(for [name ["Tobie" "Jaime"]]
                     {:create  (type/thing "person" name)
                      :content {:name name}}) 'name)
; (for
;  [$name ["Tobie" "Jaime"]]
;  {:create (type/thing "person" $name), :content {:name $name}})

  (format-for '(for [name ["Tobie" "Jaime"]]
                 {:create  (type/thing "person" name)
                  :content {:name name}})) ; "FOR $name IN ['Tobie', 'Jaime'] { CREATE clojure.lang.LazySeq@46f033f5 CONTENT {name: $name}; };"

  *e)
