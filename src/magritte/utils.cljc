(ns magritte.utils
  (:require [clojure.string :as str]))

(declare map->str)
(declare list->infix)

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

(comment
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
