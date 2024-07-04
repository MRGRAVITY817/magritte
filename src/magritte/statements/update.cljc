(ns magritte.statements.update
  (:require
   [clojure.string :as str]
   [magritte.utils :refer [->query-str list->str]]))

(defn- handle-update [update]
  (when update
    (str "UPDATE " (->query-str update))))

(defn- handle-update-only [update-only]
  (when update-only
    (str "UPDATE ONLY " (->query-str update-only))))

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

(defn- handle-unset [unset]
  (when (vector? unset)
    (str "UNSET " (->> unset
                       (map name)
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
  [{:keys [update update-only set unset]}]
  (->> [(handle-update update)
        (handle-update-only update-only)
        (handle-set set)
        (handle-unset unset)]
       (filter identity)
       (str/join " ")))
