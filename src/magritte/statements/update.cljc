(ns magritte.statements.update
  (:require
   [clojure.string :as str]
   [magritte.utils :refer [->query-str list->str]]))

(defn- handle-update [update]
  (when update
    (str "UPDATE " (->query-str update))))

(defn- handle-set [set]
  (when set
    (str "SET " (->> set
                     (map #(list->str % :no-surroundings))
                     (str/join ", ")))))

(defn format-update
  "Formats an update statement.

  ```
  UPDATE [ ONLY ] @targets
    [ CONTENT @value
      | MERGE @value
      | PATCH @value
      | [ SET @field = @value, ... | UNSET @field, ... ]
    ]
    [ WHERE @condition ]
    [ RETURN NONE | RETURN BEFORE | RETURN AFTER | RETURN DIFF | RETURN @statement_param, ... ]
    [ TIMEOUT @duration ]
    [ PARALLEL ]
  ;
  ```
  "
  [{:keys [update set]}]
  (->> [(handle-update update)
        (handle-set set)]
       (filter identity)
       (str/join " ")))
