(ns magritte.statements.create
  (:refer-clojure :exclude [set])
  (:require
   [clojure.string :as str]
   [magritte.statements.common :refer [handle-parallel handle-timeout handle-set handle-content]]
   [magritte.utils :refer [->query-str]]))

(defn- handle-create [create]
  (when create
    (let [fields (if (vector? create)
                   (str/join ", " (map ->query-str create))
                   (->query-str create))]
      (str "CREATE " fields))))

(defn- handle-create-only [create-only]
  (when create-only
    (str "CREATE ONLY " (name create-only))))

(defn- handle-return [return]
  (when return
    (let [return-value (cond
                         (vector? return) (str/join ", " (map name return))
                         (= return :none) "NONE"
                         (= return :before) "BEFORE"
                         (= return :after) "AFTER"
                         (= return :diff) "DIFF"
                         :else (name return))]
      (str "RETURN " return-value))))

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
  [{:keys [create create-only set content return timeout parallel]}]
  (->> [(handle-create create)
        (handle-create-only create-only)
        (handle-set set)
        (handle-content content)
        (handle-return return)
        (handle-timeout timeout)
        (handle-parallel parallel)]
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

