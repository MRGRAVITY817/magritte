(ns magritte.statements.for
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [magritte.statements.common :refer [replace-symbol replace-symbols]]
   [magritte.statements.format :refer [format-statement]]
   [magritte.statements.let :refer [format-let]]))

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
        for-params   (->> for-bindings (map first) (set))
        let-bindings (->> bindings
                          (filter (fn [[k _]] (= :let k)))
                          (first)
                          (second))
        let-params   (->> let-bindings
                          (partition 2)
                          (map first)
                          (set))
        let-statements (if let-bindings
                         (str (format-let (list 'let let-bindings) for-params) "\n")
                         "")
        opening-for-loops (->> for-bindings
                               (map (fn [[k v]]
                                      (str "FOR $" k " IN "
                                           (-> v
                                               (replace-symbols for-params)
                                               (format-statement {:surround-with-parens? true
                                                                  :add-semicolon?        false}))
                                           " { ")))
                               (apply str))
        params (set/union for-params let-params)
        statements    (if blocks
                        (->> blocks
                             (map #(-> %
                                       (replace-symbols params)
                                       (format-statement {:add-semicolon?        true
                                                          :surround-with-parens? false})))
                             (str/join "\n"))
                        "")

        closing-for-braces (->> " };"
                                repeat
                                (take (count for-bindings))
                                (apply str))]
    (str opening-for-loops
         let-statements
         statements
         closing-for-braces)))

(comment
  (def value (->> [[1 2] [:let ['name "Tobie"]]]
                  (filter (fn [[k _]] (and (keyword? k) (= :let k))))
                  (first)
                  (second))) ; [name "Tobie"]
  (format-let '(let value) '#{age name})

  (def my-map '{:create  (type/thing "person" name)
                :content {:name name}})

  (replace-symbol '(for [name ["Tobie" "Jaime"]]
                     {:create  (type/thing "person" name)
                      :content {:name name}}) 'name)
  (format-for '(for [name ["Tobie" "Jaime"]]
                 {:create  (type/thing "person" name)
                  :content {:name name}})) ; "FOR $name IN ['Tobie', 'Jaime'] { CREATE clojure.lang.LazySeq@46f033f5 CONTENT {name: $name}; };"
  (->> " };"
       repeat
       (take (count #{:hello :world}))
       (apply str))

  *e)
