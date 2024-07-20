(ns magritte.statements.define
  (:require
   [clojure.string :as str]
   [magritte.utils :as utils]))

(def ^:private define-types
  {:database  "DATABASE"
   :analyzer  "ANALYZER"
   :event     "EVENT"
   :field     "FIELD"
   :index     "INDEX"
   :namespace "NAMESPACE"})

(defn- handle-define [define]
  (when define
    (if (vector? define)
      (let [[def-type if-not-exists] define
            def-type (get define-types def-type)
            if-not-exists (when if-not-exists "IF NOT EXISTS")]
        (->> [def-type if-not-exists]
             (keep identity)
             (str/join " ")))
      (get define-types define))))

(defn- handle-define? [define?]
  (when define?
    (str (handle-define define?) " IF NOT EXISTS")))

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

(defn- handle-then [then format-statement]
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

(defn- handle-default [default]
  (when-not (nil? default)
    (str "DEFAULT " (utils/->query-str default))))

(defn- handle-value [value]
  (when-not (nil? value)
    (str "VALUE " (utils/->query-str value))))

(defn- handle-assert [assert]
  (when assert
    (str "ASSERT " (utils/->query-str assert))))

(defn- handle-readonly [readonly]
  (when readonly
    "READONLY"))

(defn- handle-permission [{:keys [for where] :as permission-map}]
  (let [permission (first permission-map)
        for (or for (first permission))
        where (or where (second permission))
        for-clause (if (vector? for)
                     (->> for (map name) (str/join ","))
                     (name for))
        where-clause (utils/->query-str where)]
    (str "FOR " for-clause " WHERE " where-clause)))

(defn- handle-permissions [permissions]
  (when (seq permissions)
    (let [permissions (->> permissions
                           (map handle-permission)
                           (str/join " "))]
      (str "PERMISSIONS " permissions))))

(defn- handle-columns [columns]
  (when (vector? columns)
    (let [columns (->> columns
                       (map name)
                       (str/join ", "))]
      (str "COLUMNS " columns))))

(defn- handle-unique [unique]
  (when unique "UNIQUE"))

(defn- handle-fields [fields]
  (when (vector? fields)
    (let [fields (->> fields
                      (map utils/->query-str)
                      (str/join ", "))]
      (str "FIELDS " fields))))

(defn- handle-search-analyzer [search-analyzer]
  (when search-analyzer
    (if (vector? search-analyzer)
      (let [[analyzer bm25 highlights] search-analyzer
            analyzer (name analyzer)
            bm25 (if bm25 "BM25" "")
            highlights (if highlights "HIGHLIGHTS" "")]
        (str/join " " ["SEARCH ANALYZER" analyzer bm25 highlights]))
      (str "SEARCH ANALYZER " (name search-analyzer)))))

(defn- handle-mtree [mtree]
  (when mtree
    (let [{:keys [dimension type distance capacity]} mtree
          dimension (when dimension (str "DIMENSION " dimension))
          type      (when type (str "TYPE " (str/upper-case (name type))))
          distance  (when distance (str "DIST " (str/upper-case (name distance))))
          capacity  (when capacity (str "CAPACITY " capacity))]
      (->> ["MTREE" dimension type distance capacity]
           (keep identity)
           (str/join " ")))))

(defn- handle-hnsw [hnsw]
  (when hnsw
    (let [{:keys [dimension type distance efc m]} hnsw
          dimension (when dimension (str "DIMENSION " dimension))
          type      (when type (str "TYPE " (str/upper-case (name type))))
          distance  (when distance (str "DIST " (str/upper-case (name distance))))
          efc       (when efc (str "EFC " efc))
          m         (when m (str "M " m))]
      (->> ["HNSW" dimension type distance efc m]
           (keep identity)
           (str/join " ")))))

(defn format-define
  "Formats a define database expression."
  [{:keys [define define? name on when then changefeed tokenizers
           filters type default value assert readonly
           permissions columns unique fields search-analyzer mtree hnsw]}
   format-statement]
  (->> ["DEFINE"
        (handle-define define)
        (handle-define? define?)
        (handle-name name)
        (handle-on on)
        (handle-columns columns)
        (handle-fields fields)
        (handle-search-analyzer search-analyzer)
        (handle-mtree mtree)
        (handle-hnsw hnsw)
        (handle-when when)
        (handle-then then format-statement)
        (handle-changefeed changefeed)
        (handle-tokenizers tokenizers)
        (handle-filters filters)
        (handle-type type)
        (handle-default default)
        (handle-assert assert)
        (handle-value value)
        (handle-readonly readonly)
        (handle-permissions permissions)
        (handle-unique unique)]
       (filter identity)
       (str/join " ")
       (str/trim)))
