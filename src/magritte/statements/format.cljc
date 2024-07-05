(ns magritte.statements.format
  (:require
   [magritte.statements.create :refer [format-create]]
   [magritte.statements.delete :refer [format-delete]]
   [magritte.statements.insert :refer [format-insert]]
   [magritte.statements.select :refer [format-select]]
   [magritte.statements.update :refer [format-update]]))

(defn format-statement [expr]
  (let [statement (cond
                    (map? expr)  (let [expr-keys (-> expr keys set)]
                                   (condp #(contains? %2 %1) expr-keys
                                     :select (format-select expr)
                                     :create (format-create expr)
                                     :insert (format-insert expr)
                                     :update (format-update expr)
                                     :delete (format-delete expr)
                                     (throw (ex-info "Unknown statement" {:expr expr}))))

                    :else expr)]
    (str statement ";")))
