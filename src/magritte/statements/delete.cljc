(ns magritte.statements.delete
  (:require
   [clojure.string :as str]
   [magritte.statements.common :refer [handle-parallel handle-return
                                       handle-timeout handle-where]]
   [magritte.utils :as utils]))

(defn- handle-delete [delete]
  (when delete
    (str "DELETE " (utils/->query-str delete))))

(defn- handle-delete-only [delete-only]
  (when delete-only
    (str "DELETE ONLY " (utils/->query-str delete-only))))

(defn format-delete
  "Formats an delete statement.

  ```
  DELETE [ ONLY ] @targets
    [ WHERE @condition ]
    [ RETURN NONE | RETURN BEFORE | RETURN AFTER | RETURN DIFF | RETURN @statement_param, ... ]
    [ TIMEOUT @duration ]
    [ PARALLEL ]
  ;
  ```
  "
  [{:keys [delete delete-only where return timeout parallel]}]
  (->> [(handle-delete delete)
        (handle-delete-only delete-only)
        (handle-where where)
        (handle-return return)
        (handle-timeout timeout)
        (handle-parallel parallel)]
       (filter identity)
       (str/join " ")))
