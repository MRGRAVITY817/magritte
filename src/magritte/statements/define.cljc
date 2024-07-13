(ns magritte.statements.define
  (:require
   [clojure.set :as set]
   [clojure.string :as str]))

(def ^:private define-types
  {:database "DATABASE"
   :analyzer "ANALYZER"})

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

(def Tokenizers
  "Tokenizers available for use in `DEFINE ANALYZER`."
  #{:blank :camel :class :punct})

(defn- handle-tokenizers [tokenizers]
  (when (and (vector? tokenizers)
             (set/subset? tokenizers Tokenizers))
    (let [tokenizers (->> tokenizers
                          (map name)
                          (str/join ","))]
      (str "TOKENIZERS " tokenizers))))

(defn format-define
  "Formats a define database expression."
  [{:keys [define name changefeed tokenizers]}]
  (->> ["DEFINE"
        (handle-define define)
        (handle-name name)
        (handle-changefeed changefeed)
        (handle-tokenizers tokenizers)]
       (filter identity)
       (str/join " ")
       (str/trim)))
