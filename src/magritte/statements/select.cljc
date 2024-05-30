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
    :else (let [{:keys [array index]} field]
            (if (and array index)
              (str (name array) "[" index "]")
              (name field)))))

(defn- get-alias-name
  "Get correct alias name.
   
   Example: 
   
   ```
    (get-alias-name :username) ;; => \"username\"
    (get-alias-name \"username\") ;; => \"`username`\"
   ```
  "
  [alias]
  (if (keyword? alias)
    (name alias)
    (str "`" alias "`")))

(defn- rename-field [field]
  (if (vector? field)
    (str (get-field-name (first field)) " AS " (get-alias-name (second field)))
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


