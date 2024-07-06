(ns magritte.statements.format
  (:require
   [magritte.statements.create :refer [format-create]]
   [magritte.statements.delete :refer [format-delete]]
   [magritte.statements.insert :refer [format-insert]]
   [magritte.statements.select :refer [format-select]]
   [magritte.statements.update :refer [format-update]]
   [magritte.utils :as utils]))

(defn format-statement [expr {:keys [add-semicolon? surround-with-parens?]}]
  (let [statement (cond
                    (map? expr)  (let [expr-keys (-> expr keys set)
                                       statement (condp #(contains? %2 %1) expr-keys
                                                   :select (format-select expr)
                                                   :select-value (format-select expr)
                                                   :create (format-create expr)
                                                   :insert (format-insert expr)
                                                   :update (format-update expr)
                                                   :delete (format-delete expr)
                                                   (utils/->query-str expr))]
                                   (if surround-with-parens?
                                     (str "(" statement ")")
                                     statement))
                    :else (utils/->query-str expr))
        statement (if add-semicolon? (str statement ";") statement)]
    statement))
