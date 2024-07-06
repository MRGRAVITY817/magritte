(ns magritte.statements.for
  (:require
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
          iterable (cond
                     (vector? iterable) (utils/->query-str iterable)
                     :else (format-statement iterable {:surround-with-parens? true
                                                       :add-semicolon?       false}))
          block (-> for-statement
                    (last)
                    (replace-symbol item)
                    (format-statement {:surround-with-parens? false
                                       :add-semicolon?      true}))]
      (str "FOR $" item " IN " iterable " { " block " };"))))

"FOR $person IN (SELECT VALUE id FROM person WHERE age >= 18) { UPDATE $person SET can_vote = true; };"

"FOR $person IN (SELECT VALUE id FROM person WHERE (age >= 18)) { UPDATE $person SET [can_vote, true]; };"

(comment
  (def my-map '{:create  (type/thing "person" name)
                :content {:name name}})

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
