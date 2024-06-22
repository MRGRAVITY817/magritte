(ns magritte.statements.create
  (:refer-clojure :exclude [set])
  (:require [clojure.string :as str]
            [magritte.utils :refer [to-valid-str]]))

(defn- handle-create [create]
  (str "CREATE " (to-valid-str create)))

(defn- handle-set [set]
  ;{:name    "Tobie"
  ; :company "SurrealDB"
  ; :skills  ["Rust" "Go" "JavaScript"]}
  ; => "SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript']"
  (when set
    (let [setters (for [[k v] set]
                    (str (name k) " = " (to-valid-str v)))]
      (str "SET "
           (str/join ", " setters)))))

(defn- handle-content [content]
  (when content
    (str "CONTENT {"
         (str/join ", "
                   (for [[k v] content]
                     (str (name k) ": " (to-valid-str v))))
         ",}")))

(defn format-create
  "Format create statement.

   ```
   CREATE [ ONLY ] @targets
   	[ CONTENT @value
   	  | SET @field = @value ...
   	]
   	[ RETURN NONE | RETURN BEFORE | RETURN AFTER | RETURN DIFF | RETURN @statement_param, ... ]
   	[ TIMEOUT @duration ]
   	[ PARALLEL ]
   ;
   ```
  "
  [{:keys [create set content]}]
  (->> [(handle-create create)
        (handle-set set)
        (handle-content content)]
       (filter identity)
       (str/join " ")))

(comment
  (format-create {:create :person}) ; "CREATE person"
  (format-create {:create :person:100}) ; "CREATE person:100"
  (format-create {:create :person:100
                  :set    {:name    "Tobie"
                           :company "SurrealDB"
                           :skills  ["Rust" "Go" "JavaScript"]}}) ; "CREATE person:100 SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript']"
  )

