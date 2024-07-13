(ns magritte.statements.define
  (:require
   [clojure.string :as str]
   [magritte.statements.format :refer [format-statement]]
   [magritte.utils :as utils]))

(def ^:private define-types
  {:database "DATABASE"
   :analyzer "ANALYZER"
   :event    "EVENT"})

(defn- handle-define [define]
  (when define
    (if (vector? define)
      (let [[def-type if-not-exists] define
            def-type (get define-types def-type)
            if-not-exists (if if-not-exists "IF NOT EXISTS" "")]
        (str def-type " " if-not-exists))
      (get define-types define))))

(defn- handle-name [name']
  (when name'
    (name name')))

(defn- handle-changefeed [changefeed]
  (when changefeed
    (str "CHANGEFEED " (name changefeed))))

(defn- handle-tokenizers [tokenizers]
  (when (vector? tokenizers)
    (let [tokenizers (->> tokenizers
                          (map name)
                          (str/join ","))]
      (str "TOKENIZERS " tokenizers))))

(defn- handle-filters [filters]
  (when filters
    (let [filters (->> filters
                       (map utils/->query-str)
                       (str/join ","))]
      (str "FILTERS " filters))))

(defn- handle-on-table [on-table]
  (when on-table
    (str "ON TABLE " (name on-table))))

(defn- handle-when [when']
  (when when'
    (let [when-condition (cond
                           (list? when')
                           (let [list-where (utils/list->str when')
                                 [left op right] (str/split list-where #" ")]
                             (if (= "contains?" op)
                               (str  left " CONTAINS " right)
                               list-where))

                           :else (name when'))]
      (str "WHEN " when-condition))))

(defn- handle-then [then]
  (when then
    (let [then-str (format-statement then {})
          is-single-statement (map? then)
          opening (if is-single-statement "(" "{")
          closing (if is-single-statement ")" "}")]
      (str "THEN " opening then-str closing))))

(defn format-define
  "Formats a define database expression."
  [{:keys [define name on-table when then changefeed tokenizers filters]}]
  (->> ["DEFINE"
        (handle-define define)
        (handle-name name)
        (handle-on-table on-table)
        (handle-when when)
        (handle-then then)
        (handle-changefeed changefeed)
        (handle-tokenizers tokenizers)
        (handle-filters filters)]
       (filter identity)
       (str/join " ")
       (str/trim)))
