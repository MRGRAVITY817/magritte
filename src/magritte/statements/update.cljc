(ns magritte.statements.update
  (:require
   [clojure.string :as str]
   [magritte.utils :refer [->query-str list->str]]))

(defn- handle-update [update]
  (when update
    (str "UPDATE " (->query-str update))))

(defn- handle-set-item [arg1]
  (cond
    (list? arg1) (list->str arg1 :no-brackets)
    (map? arg1)  (->> arg1
                      (map (fn [[k v]] (str (name k) " = " (->query-str v))))
                      (str/join ", "))
    :else        (->query-str arg1)))

(defn- handle-set [set]
  (when set
    (str "SET " (->> set
                     (map handle-set-item)
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
