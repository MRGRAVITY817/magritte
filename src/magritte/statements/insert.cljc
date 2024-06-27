(ns magritte.statements.insert
  (:require
   [clojure.string :as str]
   [magritte.utils :refer [->query-str]]))

(defn- handle-insert
  "Handle insert statement"

  [insert]
  (when insert
    ;; implement the logic above
    (let [fields (if (vector? insert)
                   (str/join ", " (map ->query-str insert))
                   (name insert))]
      (str "INSERT INTO " fields))))

(defn- handle-content
  "Handle content statement"
  [content]
  (when content
    (str "{"
         (str/join ", "
                   (for [[k v] content]
                     (str (name k) ": " (->query-str v))))
         "}")))

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
  [{:keys [insert content]}]
  (->> [(handle-insert insert)
        (handle-content content)]
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
