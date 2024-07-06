(ns magritte.statements.let
  (:require
   [clojure.string :as str]
   [magritte.statements.common :refer [replace-symbol]]
   [magritte.statements.format :refer [format-statement]]))

(defn- replace-symbols
  "Replace symbols in a statement."
  [statement params]
  (if (empty? params)
    statement
    (let [[param & rest] params]
      (replace-symbols (replace-symbol statement param) rest))))

(defn format-let
  "Format let statement."
  [statement]
  (let [[_ binding-list & blocks] statement
        bindings (->> binding-list
                      (partition 2))
        params   (->> bindings (map first) (set))
        bindings (->> bindings
                      (map (fn [[k v]]
                             (str "LET $" k " = "
                                  (format-statement v {:add-semicolon?        true
                                                       :surround-with-parens? true}))))
                      (str/join "\n"))
        blocks    (if blocks
                    (->> blocks
                         (map #(-> %
                                   (replace-symbols params)
                                   (format-statement {:add-semicolon?        true
                                                      :surround-with-parens? false})))
                         (str/join "\n"))
                    nil)]
    (str bindings (if blocks (str "\n" blocks) ""))))

(comment

  (->> [:a 1 :b 2 :c 3]
       (partition 2)
       (map (fn [[k v]] (str k " = " v)))
       (str/join ", "))

  ;; Get odd elements
  (->> [:a 1 :b 2 :c 3]
       (partition 2)
       (map second)
       (vec))
  (->> [:a 1 :b 2 :c 3]))
