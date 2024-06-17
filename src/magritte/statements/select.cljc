(ns magritte.statements.select
  (:require [magritte.utils :as utils]
            [clojure.string :as str]))

(declare format-select)

(defn- get-field-name
  "Get correct field name.
   
   Example: 
   
   ```
    (get-field-name :username) ;; => \"username\"
    (get-field-name {:array :username
                     :index 12}) ;; => \"username[12]\"
    (get-field-name '(+ :rating 2)) ;; => \"(rating + 2)\"
   ```
  "
  [field]
  (cond
    (list? field) (utils/list->str field)
    (map? field) (if (= (meta field) {:object true})
                   (utils/map->str field)
                   (str "(" (format-select field) ")"))
    (number? field) (str field)
    :else (name field)))

(defn- get-array-statement
  "Get array statements. 

   Array statements in SurrealQL are the statements inside the square brackets.
   For example, in the statement `SELECT address[WHERE city = 'New York']`, 
   the array statement is `WHERE city = 'New York'`."
  [field]
  (when (vector? field)
    (str "["
         (str/join " "
                   (cons (-> field first get-field-name str/upper-case)
                         (->> field rest (map get-field-name))))
         "]")))

(defn- get-alias-name
  "Get correct alias name.
   
   Example: 
   
   ```
    (get-alias-name :username) ;; => \"username\"
    (get-alias-name \"username\") ;; => \"`username`\"
   ```
  "
  [alias]
  (cond
    (keyword? alias) (name alias)
    :else (str "`" alias "`")))

(defn- get-alias [field]
  (when (not (vector? (last field)))
    (str " AS " (get-alias-name (last field)))))

(defn- rename-field [field]
  (if (and (vector? field)
           (>= (count field) 2))
    (str (get-field-name (first field))
         (get-array-statement (second field))
         (get-alias field))
    (utils/to-valid-str field)))

(defn- rename-fields [fields]
  (->> fields
       (map rename-field)
       (str/join ", ")))

(defn- handle-select [select-value select]
  (if select-value
    (str "SELECT VALUE " (name select-value))
    (str "SELECT " (if (= select [:*]) "*" (rename-fields select)))))

(defn- handle-from-field [from-field]
  (cond
    (and (vector? from-field)
         (-> from-field second utils/is-range?))
    (str (get-field-name (first from-field))
         ":"
         (utils/list->range (second from-field)))

    (and (vector? from-field)
         (every? #(not (list? %)) from-field))
    (str "[" (str/join ", " (map utils/to-valid-str from-field)) "]")

    :else (get-field-name from-field)))

(comment
  (type '[1 2 3]))

(defn- handle-from [from-only from]
  (if from-only
    (str "FROM ONLY " (name from-only))
    (str "FROM " (->> from
                      (map handle-from-field)
                      (str/join ", ")))))

(defn- handle-where [where]
  (when where
    (str "WHERE "
         (cond
           (list? where) (utils/list->str where)
           :else (name where)))))

(defn- handle-group [group]
  (when group
    (str "GROUP " (-> group name str/upper-case))))

(defn- handle-limit [limit]
  (when (integer? limit)
    (str "LIMIT "  limit)))

(defn- handle-omit [omit]
  (when omit
    (str "OMIT " (str/join ", " (map name omit)))))

; SELECT [ VALUE ] @fields [ AS @alias ]
; 	[ OMIT @fields ...]
; 	FROM [ ONLY ] @targets
; 	[ WITH [ NOINDEX | INDEX @indexes ... ]] [ WHERE @conditions ]
; 	[ SPLIT [ AT ] @field ... ]
; 	[ GROUP [ BY ] @fields ... ]
; 	[ ORDER [ BY ]
; 		@fields [
; 			RAND()
; 			| COLLATE
; 			| NUMERIC
; 		] [ ASC | DESC ] ...
; 	]
; 	[ LIMIT [ BY ] @limit ]
; 	[ START [ AT ] @start ]
; 	[ FETCH @fields ... ]
; 	[ TIMEOUT @duration ]
; 	[ PARALLEL ]
; 	[ EXPLAIN [ FULL ]]
; ;

(defn format-select [{:keys [select select-value omit from
                             from-only where group limit]}]
  (->> [(handle-select select-value select)
        (handle-omit omit)
        (handle-from from-only from)
        (handle-where where)
        (handle-group group)
        (handle-limit limit)]
       (filter identity)
       (str/join " ")
       str/trimr))

(comment
  (defn- check-array [field]
    (let [{:keys [array index]} field]
      (if (and array index)
        (str (name array) "[" index "]")
        (name field))))

  (check-array {:array :hello.world
                :index 12})
  (check-array :hello)

  (meta ^:private {:hello "world"}))


