(ns magritte.statements.select
  (:require [magritte.utils :as utils]
            [clojure.string :as str]))

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
    (list? field) (utils/list->infix field)
    (map? field) (utils/map->str field)
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
    (name field)))

(defn- rename-fields [fields]
  (->> fields
       (map rename-field)
       (str/join ", ")))

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

(defn format-select [expr]
  (let [fields (get expr :select)
        from (get expr :from)
        from-only (get expr :from-only)
        select-value (get expr :select-value)
        group (get expr :group)]

    (-> [(if select-value
           (str "SELECT VALUE " (name select-value))
           (str "SELECT " (if (= fields [:*]) "*" (rename-fields fields))))
         "FROM"
         (if from-only
           (str "ONLY " (name from-only))
           (utils/to-str-items from))
         (when group
           (str "GROUP " (-> group name str/upper-case)))]
        (#(str/join " " %))
        str/trimr
        (#(str % ";")))))

(comment
  (defn- check-array [field]
    (let [{:keys [array index]} field]
      (if (and array index)
        (str (name array) "[" index "]")
        (name field))))

  (check-array {:array :hello.world
                :index 12})
  (check-array :hello))


