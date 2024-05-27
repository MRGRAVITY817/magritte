(ns magritte.utils
  (:require [clojure.string :as str]))

(defn to-str-items [fields]
  (->> fields (map name) (str/join ", ")))

(defn to-valid-str
  "Converts an argument to a string representation suitable for use in a SurrealQL query."
  [arg]
  (cond
    (string? arg) (str "'" arg "'")
    (vector? arg) (str "[" (str/join ", " (map to-valid-str arg)) "]")
    (nil? arg) "null"
    (= :none arg) "NONE"
    :else arg))

(defn kebab->snake_name
  "Converts kebab item to a snake_case string."
  [kw]
  (->> kw
       name
       (#(str/replace % #"-" "_"))))

(comment
  (map kebab->snake_name
      ; ("camelCase" "snake_case" "kebab_case" "snake_case" "kebab_case" "camel_case")
       [:camelCase :snake-case :kebab-case :snake_case :kebab_case :camel_case]))
