(ns magritte.statements.common
  (:require
   [clojure.string :as str]
   [magritte.utils :as utils]))

(defn handle-timeout [timeout]
  (when timeout
    (let [time (cond
                 (number? timeout) (str timeout "s")
                 :else (name timeout))]
      (str "TIMEOUT " time))))

(defn handle-parallel [parallel]
  (if parallel "PARALLEL" nil))

(defn handle-where [where]
  (when where
    (str "WHERE "
         (cond
           (list? where) (let [list-where (utils/list->str where)
                               [left op right] (str/split list-where #" ")]
                           (if (= "contains?" op)
                             (str  left " CONTAINS " right)
                             list-where))
           :else (name where)))))

(defn handle-return [return]
  (when return
    (let [return-str (cond
                       (keyword? return)
                       (str/upper-case (name return))

                       (vector? return)
                       (str/join ", " (map utils/->query-str return))

                       :else (utils/->query-str return))]
      (str "RETURN " return-str))))

(defn replace-symbol [x match]
  (cond
    (symbol? x) (if (= x match) (symbol (str "$" match)) x)
    (list? x) (->> x
                   (map #(replace-symbol % match))
                   (into '())
                   (reverse))
    (map? x) (into {} (map (fn [[k v]] [k (replace-symbol v match)])) x)
    (vector? x) (vec (map #(replace-symbol % match) x))
    :else x))

(defn replace-symbols
  "Replace symbols in a statement."
  [statement params]
  (if (empty? params)
    statement
    (let [[param & rest] params]
      (replace-symbols (replace-symbol statement param) rest))))
