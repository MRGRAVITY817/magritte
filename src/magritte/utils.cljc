(ns magritte.utils
  (:require [clojure.string :as str]))

(declare map->str)

(defn to-str-items [fields]
  (->> fields (map name) (str/join ", ")))

(defn to-valid-str
  "Converts an argument to a string representation suitable for use in a SurrealQL query."
  [arg]
  (cond
    (string? arg) (str "'" arg "'")
    (vector? arg) (str "[" (str/join ", " (map to-valid-str arg)) "]")
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
  "Convert clojure map to string to be used in query"
  [map-entry]
  (let [map-item (first map-entry)]
    (str "{" (-> map-item first name) ": " (-> map-item second to-valid-str) "}")))

(defn list->infix
  "Converts a list with prefix notation to infix notation."
  [expr]
  (if (list? expr)
    (let [operator (first expr)
          operands (rest expr)]
      (str "(" (str/join (str " " operator " ") (map list->infix operands)) ")"))
    (str expr)))

(comment
  (map kebab->snake_name
       [:camelCase :snake-case :kebab-case :snake_case :kebab_case :camel_case])
  (def new-map {:id "hello"})
  (def map-list [{:id "hello"} {:name "world"}])
  (-> {:id "hello"} first map->str)
  (to-valid-str map-list)
  (map map->str new-map)
  (list->infix '(+ 1 2))
  (list->infix '(* 1 2))
  (list->infix '(/ 1 2))
  (list->infix '(- 1 2))
  ;; More complicated
  (list->infix '(* (+ 1 2) (- 3 4)))
  (list->infix '(* (+ 1 2) (- 3 4) (/ 5 6))))
