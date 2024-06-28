(ns magritte.statements.insert
  (:require
   [clojure.string :as str]
   [magritte.utils :refer [->query-str list->str]]))

(defn- handle-insert
  "Handle insert statement"

  [insert]
  (when insert
    ;; implement the logic above
    (let [fields (if (vector? insert)
                   (let [table (name (first insert))
                         fields (map name (second insert))]
                     (str table " (" (str/join ", " fields) ")"))
                   (name insert))]
      (str "INSERT INTO " fields))))

(comment
  (handle-insert :person)
  (handle-insert [:company [:name :founded]])
  ;
  )
(defn- handle-content
  "Handle content statement"
  [content]
  (when content
    (str "{"
         (str/join ", "
                   (for [[k v] content]
                     (str (name k) ": " (->query-str v))))
         "}")))

(defn- handle-one-value
  [value]
  (str "(" (str/join ", " (map ->query-str value)) ")"))

(comment
  (handle-one-value ["SurrealDB" "2021-09-10"]))

(defn- handle-values
  "Handle VALUES statement inside INSERT statement."
  [values]
  (let [insert-values (if (vector? (first values))
                        (->> values
                             (map handle-one-value)
                             (str/join ", "))
                        (handle-one-value values))]
    (when values
      (str "VALUES " insert-values))))

(defn- handle-dupdate
  "Handle ON DUPLICATE KEY UPDATE clause"
  [dupdate]
  (when (and (vector? dupdate)
             (list? (first dupdate)))
    (str "ON DUPLICATE KEY UPDATE "
         (->> dupdate
              (map #(list->str % :no-surroundings))
              (str/join ", ")))))

(defn format-insert
  "Format insert statement.

   ```
   INSERT [ IGNORE ] INTO @what
   	[ @value
   	  | (@fields) VALUES (@values)
   		[ ON DUPLICATE KEY UPDATE @field = @value ... ]
   	]
   ;
   ```
  "
  [{:keys [insert content values dupdate]}]
  (->> [(handle-insert insert)
        (handle-content content)
        (handle-values values)
        (handle-dupdate dupdate)]
       (filter identity)
       (str/join " ")))

(comment
  (format-insert {:insert  :company
                  :content {:name     "SurrealDB"
                            :founded  "2021-09-10"
                            :founders [:person:tobie :person:jaime]
                            :tags     ["big data" "database"]}}) ; 
  *e
  :rcf)
