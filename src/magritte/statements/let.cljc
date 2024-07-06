(ns magritte.statements.let
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [magritte.statements.common :refer [replace-symbols]]
   [magritte.statements.format :refer [format-statement]]))

(defn format-let
  "Format let statement."
  ([statement]
   (format-let statement #{}))
  ([statement params]
   (let [[_ binding-list & blocks] statement
         bindings (->> binding-list
                       (partition 2))
         params   (->> bindings
                       (map first)
                       (set)
                       (set/union params))
         bindings (->> bindings
                       (map (fn [[k v]]
                              (str "LET $" k " = "
                                   (format-statement (replace-symbols v params)
                                                     {:add-semicolon?        true
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
     (str bindings (if blocks (str "\n" blocks) "")))))

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
