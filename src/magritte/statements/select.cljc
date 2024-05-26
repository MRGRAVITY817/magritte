(ns magritte.statements.select
  (:require [magritte.utils :as utils]
            [clojure.string :as str]))

(defn- rename-field [field]
  (if (vector? field)
    (str (name (first field)) " AS " (name (second field)))
    (name field)))

(defn- rename-fields [fields]
  (->> fields
       (map rename-field)
       (str/join ", ")))

; SELECT [ VALUE ] @fields [ AS @alias ]
; 	[ OMIT @fields ...]
; 	FROM [ ONLY ] @targets
; 	[ WITH [ NOINDEX | INDEX @indexes ... ]]
; 	[ WHERE @conditions ]
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
  ;; {:select {:fields [:*] :from [:person]}}
  ;; -> "SELECT * from person;"
  (let [fields (get expr :select)
        from (get expr :from)
        from-only (get expr :from-only)
        select-value (get expr :select-value)]
    (str
     (if select-value
       (str "SELECT VALUE " (name select-value))
       (str "SELECT " (if (= fields [:*]) "*" (rename-fields fields))))
     " FROM "
     (if from-only
       (str "ONLY " (name from-only))
       (utils/to-str-items from))
     ";")))

(comment
  (rename-fields [:name :address :email])
  (rename-fields [[:name :username] :address]))
