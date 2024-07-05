(ns magritte.statements.update
  (:require
   [clojure.string :as str]
   [magritte.statements.common :refer [handle-where]]
   [magritte.utils :as utils]))

(defn- handle-update [update]
  (when update
    (str "UPDATE " (utils/->query-str update))))

(defn- handle-update-only [update-only]
  (when update-only
    (str "UPDATE ONLY " (utils/->query-str update-only))))

(defn- handle-set-item [arg1]
  (cond
    (list? arg1) (utils/list->str arg1 :no-brackets)
    (map? arg1)  (->> arg1
                      (map (fn [[k v]] (str (name k) " = " (utils/->query-str v))))
                      (str/join ", "))
    :else        (utils/->query-str arg1)))

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

(defn- handle-content [content]
  (when content
    (str "CONTENT " (utils/->query-str content))))

(defn- handle-merge [merge]
  (when merge
    (str "MERGE " (utils/->query-str merge))))

(defn- handle-patch [patch]
  (when (vector? patch)
    (let [jsons (->> patch
                     (map utils/map->json))
          patch-str (str "[" (str/join ", " jsons) "]")]
      (str "PATCH " patch-str))))

(defn- handle-return [return]
  (when return
    (let [return-str (cond
                       (keyword? return)
                       (str/upper-case (name return))

                       (vector? return)
                       (str/join ", " (map utils/->query-str return))

                       :else (utils/->query-str return))]
      (str "RETURN " return-str))))

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
  [{:keys [update update-only content merge patch set unset where return]}]
  (->> [(handle-update update)
        (handle-update-only update-only)
        (handle-content content)
        (handle-merge merge)
        (handle-patch patch)
        (handle-set set)
        (handle-unset unset)
        (handle-where where)
        (handle-return return)]
       (filter identity)
       (str/join " ")))
