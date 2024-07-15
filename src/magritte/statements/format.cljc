(ns magritte.statements.format
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [magritte.statements.common :refer [replace-symbols]]
   [magritte.statements.create :refer [format-create]]
   [magritte.statements.define :refer [format-define]]
   [magritte.statements.delete :refer [format-delete]]
   [magritte.statements.insert :refer [format-insert]]
   [magritte.statements.relate :refer [format-relate]]
   [magritte.statements.select :refer [format-select]]
   [magritte.statements.transaction :refer [format-begin format-cancel
                                            format-commit]]
   [magritte.statements.update :refer [format-update]]
   [magritte.utils :as utils]))

(declare format-let)
(declare format-for)
(declare format-if)
(declare format-when)
(declare format-cond)
(declare format-do)
(declare format-condp)
(declare format-break)
(declare format-continue)

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
                                                   :relate (format-relate expr)
                                                   :define (format-define expr format-statement)
                                                   (utils/->query-str expr))]
                                   (if surround-with-parens?
                                     (str "(" statement ")")
                                     statement))
                    ;; TODO: Refactor this to use a map or `or`
                    (list? expr)  (let [statement (condp = (first expr)
                                                    'let (format-let expr)
                                                    'do (format-do expr)
                                                    'for (format-for expr)
                                                    'if  (format-if expr)
                                                    'when (format-when expr)
                                                    'cond (format-cond expr)
                                                    'condp (format-condp expr)
                                                    'break (format-break expr)
                                                    'continue (format-continue expr)
                                                    'begin (format-begin expr)
                                                    'begin-transaction (format-begin expr)
                                                    'cancel (format-begin expr)
                                                    'cancel-transaction (format-cancel expr)
                                                    'commit (format-commit expr)
                                                    'commit-transaction (format-commit expr)
                                                    (utils/->query-str expr))]
                                    statement)
                    :else (utils/->query-str expr))
        statement (if add-semicolon? (str statement ";") statement)]
    statement))

;; Helper functions

(defn format-do
  "Format a do statement."
  [[fn-name & statements]]
  (when (and  (= fn-name 'do)
              (seq? statements))
    (str (->> statements
              (map #(format-statement % {:add-semicolon? true}))
              (str/join "\n")) "\n")))

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
                                       (format-statement {:add-semicolon?        (not (list? %))
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

(defn format-if
  "Format an if statement."
  [[_ condition if-then else-then]]
  (let [condition (format-statement condition {:surround-with-parens? true})
        if-then   (format-statement if-then {:surround-with-parens? false})
        else-then (when else-then (format-statement else-then {:surround-with-parens? false}))]
    (str "IF " condition " { " if-then " }"
         (if else-then (str " ELSE { " else-then " }") "")
         ";")))

(defn format-when
  "Map a `when` function to IF statement.
   Like Clojure's `when` function, this function will only return the `then` part of the statement.
   "
  [statement]
  (format-if (take 3 statement)))

(defn format-cond
  "Map a `cond` function to IF statement.
   Like Clojure's `cond` function, it contains a list of conditions and their corresponding statements.
   
   ```
   (format-cond 
     (cond
       (= 9 9) \"Nine is indeed nine\"
       (= 9 8) \"Nine is not nine\"
       :else   \"Nine is not nine\"))

   ;; => \"IF (9 = 9) { 'Nine is indeed nine' } ELSE IF (9 = 8) { 'Nine is not nine' } ELSE { 'Nine is not nine' };\"
   ```
   "
  [[fn-name & branches]]
  (when (and (= fn-name 'cond)
             (even? (count branches)))
    (let [branches (->> (partition 2 branches)
                        (map-indexed (fn [idx [condition statement]]
                                       (str (cond
                                              (= idx 0) "IF "
                                              (= :else condition) " ELSE"
                                              :else " ELSE IF ")
                                            (when (not= :else condition)
                                              (format-statement condition {:surround-with-parens? true}))
                                            " { "
                                            (format-statement statement {:surround-with-parens? false})
                                            " }"))))]
      (str (str/join "" branches) ";"))))

(defn format-condp [[fn-name operator operand & branches]]
  (when (and (= fn-name 'condp)
             operator operand)
    (let [else-branch (if (odd? (count branches))
                        (str " ELSE { "
                             (format-statement (last branches) {:surround-with-parens? false})
                             " }")
                        "")
          branches (partition 2 branches)

          branches (->> branches
                        (map-indexed (fn [idx [condition statement]]
                                       (str (cond
                                              (= idx 0) "IF "
                                              :else " ELSE IF ")
                                            (when (not= :else condition)
                                              (utils/list->str `(~operator ~condition ~operand)))
                                            " { "
                                            (format-statement statement {:surround-with-parens? false})
                                            " }"))))]

      (str (str/join "" branches) else-branch ";"))))

(defn format-break [expr]
  (when (= expr '(break))
    "BREAK"))

(defn format-continue [expr]
  (when (= expr '(continue))
    "CONTINUE"))
