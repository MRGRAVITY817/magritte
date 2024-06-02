(ns magritte.utils
  (:require [clojure.string :as str]
            [clojure.repl :as repl]))

(declare map->str)
(declare graph->str)
(declare list->infix)
(declare list->infix)
(declare list->db-fn)
(declare to-valid-str)

(defn to-str-items [fields]
  (->> fields (map name) (str/join ", ")))

(defn to-valid-str
  "Converts an argument to a string representation suitable for use in a SurrealQL query."
  [arg]
  (cond
    (string? arg) (str "'" arg "'")
    (vector? arg) (str "[" (str/join ", " (map to-valid-str arg)) "]")
    (list? arg) (list->infix arg)
    (map? arg) (map->str arg)
    (nil? arg) "null"
    (= :none arg) "NONE"
    (keyword? arg) (name arg)
    :else arg))

(defn kebab->snake_name
  "Converts kebab item to a snake_case string."
  [kw]
  (->> kw
       name
       (#(str/replace % #"-" "_"))))

(defn map->str
  "Convert clojure map to string to be used in query
  
   Example:

   ```
   (map-str {:id \"hello\" :name \"world\"})
   ;; => \"{id: \"hello\", name: \"world\"}\"

   (map-str {:rating '(+ :rating 2) :name \"world\"})
   ;; => \"{rating: (rating + 2), name: \"world\"}\"
   ```
  "
  [map-entry]
  (str "{"
       (str/join ", "
                 (for [[k v] map-entry]
                   (str (name k) ": " (to-valid-str v))))
       "}"))

(defn list->str
  "Converts a list to a string representation suitable for use in a SurrealQL query."
  [expr]
  (let [operator (first expr)]
    (cond
      (= operator '->) (graph->str expr)
      (and (symbol? operator) (namespace operator)) (list->db-fn expr)
      :else (list->infix expr))))

(defn symbol->db-fn-name
  "Builds a database function name from given namespaced symbol.
   
   Example:
   ```
   (build-db-fn-name 'time/now)
   ;; => \"time::now()\"
   ```
  "
  [expr]
  (let [[category fn-name] (-> expr str (str/split #"/"))
        fn-name (-> fn-name (str/replace #"-" "::"))]
    (str category "::" fn-name)))

(defn list->db-fn
  "Converts list to a database function string representation.
   
   Example:
   ```
   (list->db-fn '(time/now))
   ;; => \"time::now()\"
   ```
  "
  [expr]
  (str (->> expr first symbol->db-fn-name)
       "(" (str/join ", " (map to-valid-str (rest expr))) ")"))

(comment
  (symbol->db-fn-name 'time/now)
  (list->db-fn '(time/now))
  (list->db-fn '(time/floor "2021-11-01T08:30:17+00:00" :1w))
  (list->db-fn '(array/append [1 2 3] 4))
  (list->db-fn '(array/boolean-and ["true" "false" 1 1] ["true" "true" 0 "true"])))

(defn list->infix
  "Converts a list with prefix notation to infix notation.
   If the operand is a keyword, it will be converted to a string."
  [expr]
  (cond
    (list? expr) (let [operator (first expr)
                       operands (rest expr)]
                   (str "("
                        (str/join (str " " operator " ")
                                  (map list->infix operands))
                        ")"))
    :else (to-valid-str expr)))

(defn graph-item->str
  "Converts a graph item to a string."
  [graph]
  (cond
    (vector? graph) (if (= (first graph) :where)
                      (str "WHERE " (str/join " " (map graph-item->str (rest graph))))
                      (str "(" (str/join " " (map graph-item->str graph)) ")"))
    (list? graph) (list->infix graph)
    (keyword? graph) (name graph)
    :else (str graph)))

(defn graph->str
  "Converts a graph to a string."
  [graph]
  (str "->"
       (str/join "->" (map graph-item->str (rest graph)))))

(defn range-map->str
  "Converts a range map to a string.
   
   Example: 
   ```
   (range-map->str {:> 2 :< 5})
   ;; => \"2..5\"

   (range-map->str {:> 2})
   ;; => \"2..\"

   (range-map->str {:< 5})
   ;; => \"..5\"

   (range-map->str {:>= 2 :<= 5})
   ;; => \"2=..=5\"
   ```
  "
  [range-map]
  (str (when (range-map :>) (str (-> range-map :> to-valid-str)))
       (when (range-map :>=) (str (-> range-map :>= to-valid-str) "="))
       ".."
       (when (range-map :<=) (str "=" (-> range-map :<= to-valid-str)))
       (when (range-map :<) (str (-> range-map :< to-valid-str)))))

(comment
  (repl/doc to-valid-str)
  (range-map->str {:> 2 :< 5})
  (range-map->str {:> 2})
  (range-map->str {:< 5})
  (range-map->str {:>= 2 :<= 5})
  (range-map->str {:> 2 :<= 5})
  (graph->str '(-> :person :user :name))
  (map kebab->snake_name
       [:camelCase :snake-case :kebab-case :snake_case :kebab_case :camel_case])
  (def new-map {:id "hello"})
  (def map-list [{:id "hello"} {:name "world"}])
  (-> {:id "hello" :name "good"} map->str)
  (to-valid-str map-list)
  (map map->str new-map)
  (list->infix '(+ 1 2))
  (list->infix '(* 1 2))
  (list->infix '(/ 1 2))
  (list->infix '(- 1 2))
  ;; More complicated
  (list->infix '(* (+ 1 2) (- 3 4)))
  (list->infix '(* (+ 1 2) (- 3 4) (/ 5 6))))
