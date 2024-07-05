(ns magritte.statements.format
  (:require
   [magritte.statements.create :refer [format-create]]
   [magritte.statements.delete :refer [format-delete]]
   [magritte.statements.insert :refer [format-insert]]
   [magritte.statements.select :refer [format-select]]
   [magritte.statements.update :refer [format-update]]
   [magritte.utils :as utils]))

(defn format-statement [expr]
  (let [statement (cond
                    (map? expr)  (let [expr-keys (-> expr keys set)]
                                   (prn expr-keys)
                                   (condp #(contains? %2 %1) expr-keys
                                     :select (format-select expr)
                                     :select-value (format-select expr)
                                     :create (format-create expr)
                                     :insert (format-insert expr)
                                     :update (format-update expr)
                                     :delete (format-delete expr)
                                     (throw (ex-info "Unknown statement" {:expr expr}))))

                    :else (utils/->query-str expr))]
    (str statement ";")))
