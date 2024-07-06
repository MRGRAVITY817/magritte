(ns magritte.statements.format
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [magritte.statements.common :refer [replace-symbols]]
   [magritte.statements.create :refer [format-create]]
   [magritte.statements.delete :refer [format-delete]]
   [magritte.statements.insert :refer [format-insert]]
   [magritte.statements.select :refer [format-select]]
   [magritte.statements.update :refer [format-update]]
   [magritte.utils :as utils]))

(declare format-let)
(declare format-for)

(defn format-statement
  "Format a statement"
  [expr {:keys [add-semicolon? surround-with-parens?]}]
  (let [statement (cond
                    (map? expr)  (let [expr-keys (-> expr keys set)
                                       statement (condp #(contains? %2 %1) expr-keys
                                                   :select (format-select expr)
                                                   :select-value (format-select expr)
                                                   :create (format-create expr)
                                                   :insert (format-insert expr)
                                                   :update (format-update expr)
                                                   :delete (format-delete expr)
                                                   (utils/->query-str expr))]
                                   (if surround-with-parens?
                                     (str "(" statement ")")
                                     statement))
                    (list? expr)  (let [statement (condp = (first expr)
                                                    'let (format-let expr)
                                                    'for (format-for expr)
                                                    (utils/->query-str expr))]
                                    statement)
                    :else (utils/->query-str expr))
        statement (if add-semicolon? (str statement ";") statement)]
    statement))

(defn format-let
  "Format let statement.
   
   Given a let statement, format it as a string.
   Additionally you can provide a set of parameters from the outer scope.
   ```
   (format-let '(let [name \"tobie\"]))

   (format-let '(let [name \"tobie\"]
                  {:create :person
                   :set    {:name name}}))

   ;; with outer scope parameters
   (format-let '(let [age   {:select [:age]
                             :from   [:person]
                             :where  (= :name name)}
                      adult true]
                  {:create :person
                   :set    {:name name
                            :age  age
                            :adult adult}}) 
               #{'name 'age})
   ```
  "
  ([statement]
   (format-let statement #{}))
  ([statement params]
   (let [[_ binding-list & blocks] statement
         bindings (->> binding-list
                       (partition 2))
         params   (->> bindings
                       (map first)
                       (set)
                       (set/union params))
         bindings (->> bindings
                       (map (fn [[k v]]
                              (str "LET $" k " = "
                                   (format-statement (replace-symbols v params)
                                                     {:add-semicolon?        true
                                                      :surround-with-parens? true}))))
                       (str/join "\n"))
         blocks    (if blocks
                     (->> blocks
                          (map #(-> %
                                    (replace-symbols params)
                                    (format-statement {:add-semicolon?        (not (list? %))
                                                       :surround-with-parens? false})))
                          (str/join "\n"))
                     nil)]
     (str bindings (if blocks (str "\n" blocks) "")))))

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
