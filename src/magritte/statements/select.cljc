(ns magritte.statements.select
  (:require [magritte.utils :as utils]))

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
  (let [select-expr (:select expr)
        fields (get select-expr :fields)
        from (get select-expr :from)]
    (str "SELECT "
         (utils/to-str-items fields)
         " FROM "
         (utils/to-str-items from)
         ";")))

(comment
  ;; ADD test
  )
