(ns magritte.utils
  (:require
   [clojure.repl :as repl]
   [clojure.set :as set]
   [clojure.string :as str])
  (:import
   [java.util.regex Pattern]))

(declare map->str)
(declare list->graph)
(declare list->str)
(declare list->infix)
(declare is-range?)
(declare list->range)
(declare list->db-fn)
(declare keyword->str)
(declare ->query-str)

(defn ->query-str
  "Converts an argument to a string representation suitable for use in a SurrealQL query."
  ([arg]
   (cond
     (string? arg) (str "'" (str/replace arg #"'" "\\'") "'")
     (instance? Pattern arg) (str "/" (.pattern arg) "/")
     (vector? arg) (str "[" (str/join ", " (map ->query-str arg)) "]")
     (list? arg) (list->str arg)
     (map? arg) (map->str arg)
     (nil? arg) "null"
     (= :none arg) "NONE"
     (keyword? arg) (keyword->str arg)
     :else arg))
  ([arg _]
   (cond
     (string? arg) (str "\"" arg "\"")
     :else (->query-str arg))))

(defn keyword->str [arg]
  (let [s (name arg)]
    (if (or (str/includes? s "->") (str/includes? s "<-"))
      s
      (-> s
          (clojure.string/replace #"<([^>]+)>" "<$1> ")
          clojure.string/trim))))

(comment
  (keyword->str :<-person->)
  (keyword->str :->purchased->product<-purchased<-person->purchased->product))

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
                   (str (name k) ": " (->query-str v))))
       "}"))

(defn- db-fn? [operator]
  (and (symbol? operator)
       (or (set/subset? #{operator} #{'count 'rand 'ngram 'edgengram 'snowball}) ;; The only db function without namespace
           (namespace operator))))

(defn- list->props
  [[operator & rest']]
  (cond
    (and (keyword? operator)
         (= 1 (count rest')))
    (str (->query-str (first rest')) "." (name operator))

    (and (= operator 'get)
         (= 2 (count rest')))
    (str (-> rest' first ->query-str) "." (-> rest' second name))

    (let [nested-props (second rest')]
      (and (= operator 'get-in)
           (= 2 (count rest'))
           (vector? nested-props)
           (every? keyword? nested-props)))
    (->> (second rest')
         (map name)
         (str/join ".")
         (str (-> rest' first ->query-str) "."))

    (and (= operator '->)
         (symbol? (first rest'))
         (every? keyword? (rest rest')))
    (->> rest' (map name) (str/join "."))
    :else nil))

(defn list->inverse [expr]
  (when (and (= (first expr) 'not)
             (= 2 (count expr)))
    (str "!" (->query-str (second expr)))))

(defn list->throw [expr]
  (when (and (= (first expr) 'throw)
             (= 2 (count expr)))
    (str "THROW " (->query-str (second expr)))))

(defn list->str
  "Converts a list to a string representation suitable for use in a SurrealQL query."
  ([expr]
   (or
    (list->props expr)
    (list->graph expr)
    (list->inverse expr)
    (list->throw expr)
    (list->range expr)
    (list->db-fn expr)
    (list->infix expr)))
  ([expr _]
   (->> (list->str expr)
        (re-seq #"^\((.*)\)$")
        (first)
        (second))))

(comment
  (re-seq #"^\((.*)\)$" "(+ 1 2)")
  (list->str '(+ 1 2) true))

(defn symbol->db-fn-name
  "Builds a database function name from given namespaced symbol.
   
   Example:
   ```
   (build-db-fn-name 'time/now)
   ;; => \"time::now()\"
   ```
  "
  [expr]
  (let [[category fn-name] (-> expr str (str/split #"/"))]
    (if (str/blank? fn-name)
      (str category)
      (let [fn-name (-> fn-name (str/replace #"-" "::"))]
        (str category "::" fn-name)))))

(defn list->db-fn
  "Converts list to a database function string representation.
   
   Example:
   ```
   (list->db-fn '(time/now))
   ;; => \"time::now()\"
   ```
  "
  [[operator & rest']]
  (when (db-fn? operator)
    (str (symbol->db-fn-name operator)
         "(" (str/join ", " (map ->query-str rest')) ")")))

(comment
  (symbol->db-fn-name 'time/now)

  (list->db-fn '(time/now))
  (list->db-fn '(time/floor "2021-11-01T08:30:17+00:00" :1w))
  (list->db-fn '(array/append [1 2 3] 4))
  (list->db-fn '(array/boolean-and ["true" "false" 1 1] ["true" "true" 0 "true"])))

(defn get-operator [expr]
  (if (set/subset? #{expr} #{'or 'and 'allinside})
    (-> expr name str/upper-case)
    (-> expr name)))

(defn list->infix
  "Converts a list with prefix notation to infix notation.
   If the operand is a keyword, it will be converted to a string."
  [expr]
  (let [operator (-> expr first get-operator)
        operands (rest expr)]
    (str "("
         (str/join (str " " operator " ")
                   (map ->query-str operands))
         ")")))

(defn is-range?
  "Checks if the given list is a range expression."
  [expr]
  (some #(= (first expr) %) '[.. ..= =..]))

(defn list->range
  "Converts list to a range string representation.
   
   Example:
   ```
   (list->range '(.. 1 1000))
   ;; => \"1..1000\"

   (list->range '(..= [\"London\" :none] [\"London\" (time/now)]))
   ;; => \"['London', NONE]..=['London', time::now()]\""
  [[operator start end :as expr]]
  (when (is-range? expr)
    (str (when start (->query-str start))
         (str operator)
         (when end (->query-str end)))))

(defn graph-item->str
  "Converts a graph item to a string."
  [graph]
  (cond
    (vector? graph) (if (= (first graph) :where)
                      (str "WHERE " (str/join " " (map graph-item->str (rest graph))))
                      (str "(" (str/join " " (map graph-item->str graph)) ")"))
    (list? graph) (list->str graph)
    (keyword? graph) (name graph)
    :else (str graph)))

(defn list->graph
  "Converts a graph to a string."
  [[operator & graph]]
  (when  (set/subset? #{operator} #{'>-> '|->})
    (str (if (= operator '|->) "->" "")
         (str/join "->" (map graph-item->str graph)))))

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
  (str (when (range-map :>) (str (-> range-map :> ->query-str)))
       (when (range-map :>=) (str (-> range-map :>= ->query-str) "="))
       ".."
       (when (range-map :<=) (str "=" (-> range-map :<= ->query-str)))
       (when (range-map :<) (str (-> range-map :< ->query-str)))))

(defn map->json [arg1]
  (cond
    (map? arg1)  (str "{"
                      (->> arg1
                           (map (fn [[k v]] (str (str "\"" (name k) "\"")
                                                 ":"
                                                 (->query-str v :double-quote))))
                           (str/join ","))
                      "}")
    :else        (->query-str arg1)))

(comment
  (repl/doc ->query-str)
  (map->json {:id "hello" :name "world"}) ; "{\"id\":\"hello\",\"name\":\"world\"}"
  (range-map->str {:> 2 :< 5})
  (range-map->str {:> 2})
  (range-map->str {:< 5})
  (range-map->str {:>= 2 :<= 5})
  (range-map->str {:> 2 :<= 5})
  (list->graph '(-> :person :user :name))
  (map kebab->snake_name
       [:camelCase :snake-case :kebab-case :snake_case :kebab_case :camel_case])
  (def new-map {:id "hello"})
  (def map-list [{:id "hello"} {:name "world"}])
  (-> {:id "hello" :name "good"} map->str)
  (->query-str map-list)
  (map map->str new-map)
  (list->infix '(+ 1 2))
  (list->infix '(* 1 2))
  (list->infix '(/ 1 2))
  (list->infix '(- 1 2))
  ;; More complicated
  (list->infix '(* (+ 1 2) (- 3 4)))
  (list->infix '(* (+ 1 2) (- 3 4) (/ 5 6))))



