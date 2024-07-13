(ns magritte.statements.define
  (:require
   [clojure.string :as str]
   [magritte.statements.format :refer [format-statement]]
   [magritte.utils :as utils]))

(def ^:private define-types
  {:database "DATABASE"
   :analyzer "ANALYZER"
   :event    "EVENT"
   :field    "FIELD"})

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

(defn- handle-on [on]
  (when on
    (if (and (vector? on) (= (first on) :table))
      (let [[_ table] on]
        (str "ON TABLE " (name table)))
      (str "ON " (name on)))))

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

(defn- handle-type [type']
  (when type'
    (if (and (vector? type') (= (first type') :flexible))
      (let [[_ type'] type']
        (str "FLEXIBLE TYPE " (name type')))
      (str "TYPE " (name type')))))

(defn format-define
  "Formats a define database expression."
  [{:keys [define name on when then changefeed tokenizers filters type]}]
  (->> ["DEFINE"
        (handle-define define)
        (handle-name name)
        (handle-on on)
        (handle-when when)
        (handle-then then)
        (handle-changefeed changefeed)
        (handle-tokenizers tokenizers)
        (handle-filters filters)
        (handle-type type)]
       (filter identity)
       (str/join " ")
       (str/trim)))
