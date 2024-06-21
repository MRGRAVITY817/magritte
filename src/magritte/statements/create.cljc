(ns magritte.statements.create
  (:require [clojure.string :as str]
            [magritte.utils :refer [to-valid-str]]))

(defn- handle-create [create]
  (str "CREATE " (to-valid-str create)))

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
  [{:keys [create]}]
  (->> [(handle-create create)]
       identity
       (str/join " ")))
