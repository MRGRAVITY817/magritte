(ns magritte.utils
  (:require [clojure.string :as str]))

(declare map->str)

(defn to-str-items [fields]
  (->> fields (map name) (str/join ", ")))

(defn to-valid-str
  "Converts an argument to a string representation suitable for use in a SurrealQL query."
  [arg]
  (cond
    (string? arg) (str "'" arg "'")
    (vector? arg) (str "[" (str/join ", " (map to-valid-str arg)) "]")
    (map? arg) (map->str arg)
    (nil? arg) "null"
    (= :none arg) "NONE"
    :else arg))

(defn kebab->snake_name
  "Converts kebab item to a snake_case string."
  [kw]
  (->> kw
       name
       (#(str/replace % #"-" "_"))))

(defn map->str
  "Convert clojure map to string to be used in query"
  [map-entry]
  (let [map-item (first map-entry)]
    (str "{" (-> map-item first name) ": " (-> map-item second to-valid-str) "}")))

(comment
  (map kebab->snake_name
      ; ("camelCase" "snake_case" "kebab_case" "snake_case" "kebab_case" "camel_case")
       [:camelCase :snake-case :kebab-case :snake_case :kebab_case :camel_case])

  (def new-map {:id "hello"})

  (def map-list [{:id "hello"} {:name "world"}])

  (-> {:id "hello"} first map->str)

  (to-valid-str map-list)

  (map map->str new-map))
