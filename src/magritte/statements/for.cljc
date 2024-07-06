(ns magritte.statements.for
  (:require
   [clojure.string :as str]
   [magritte.statements.common :refer [replace-symbol replace-symbols]]
   [magritte.statements.format :refer [format-statement]]))

(defn format-for
  "
  Format a for loop statement.

  ```
  FOR @item IN @iterable @block
  ```
  "
  [for-statement]
  (let [[_ binding-list & blocks] for-statement
        bindings (->> binding-list
                      (partition 2))
        for-bindings (filter (fn [[k _]] (not (keyword? k))) bindings)
        let-bindings (->> bindings
                          (filter (fn [[k _]] (and (keyword? k) (= :let k))))
                          (first)
                          (second))
        params   (->> for-bindings (map first) (set))
        bindings (->> for-bindings
                      (map (fn [[k v]]
                             (str "FOR $" k " IN " (-> v
                                                       (replace-symbols params)
                                                       (format-statement {:surround-with-parens? true
                                                                          :add-semicolon?        false}))
                                  " { ")))
                      (apply str))
        blocks    (if blocks
                    (->> blocks
                         (map #(-> %
                                   (replace-symbols params)
                                   (format-statement {:add-semicolon?        true
                                                      :surround-with-parens? false})))
                         (str/join "\n"))
                    nil)
        closing-braces (->> " };"
                            repeat
                            (take (count for-bindings))
                            (apply str))]
    (str bindings (or blocks "") closing-braces)))

(comment
  (->> [[1 2] [:iflet ['name "Tobie"]]]
       (filter (fn [[k _]] (and (keyword? k) (= :let k))))
       (first)
       (second))

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
  (->> " };"
       repeat
       (take (count #{:hello :world}))
       (apply str))

  *e)
