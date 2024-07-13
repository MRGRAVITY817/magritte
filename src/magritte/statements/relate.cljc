(ns magritte.statements.relate
  (:require
   [clojure.string :as str]
   [magritte.statements.common :refer [handle-content handle-parallel
                                       handle-return handle-set handle-timeout]]
   [magritte.utils :as utils]))

(defn- handle-relate [relate]
  (when relate
    (str "RELATE " (utils/->query-str relate))))

(defn- handle-relate-only [relate-only]
  (when relate-only "ONLY"))

(defn format-relate
  "Format relate statement.

   ```
    RELATE [ ONLY ] @from_record -> @table -> @to_record
      [ CONTENT @value
        | SET @field = @value ...
      ]
      [ RETURN NONE | RETURN BEFORE | RETURN AFTER | RETURN DIFF | RETURN @statement_param, ... ]
      [ TIMEOUT @duration ]
      [ PARALLEL ]
    ;
   ```
  "
  [{:keys [relate relate-only set content return timeout parallel]}]
  (->> [(handle-relate relate)
        (handle-relate-only relate-only)
        (handle-set set)
        (handle-content content)
        (handle-return return)
        (handle-timeout timeout)
        (handle-parallel parallel)]
       (filter identity)
       (str/join " ")))
