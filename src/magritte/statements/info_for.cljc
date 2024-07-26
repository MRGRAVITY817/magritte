(ns magritte.statements.info-for
  (:require [clojure.string :as str]))

(defn- handle-info-for [info-for]
  (when info-for
    (str "INFO FOR "
         (cond
           (= :root info-for) "ROOT"
           (= :ns info-for) "NS"
           (= :namespace info-for) "NAMESPACE"
           (= :db info-for) "DB"
           (= :database info-for) "DATABASE"

           (map? info-for)
           (let [[key' value'] (first info-for)]
             (cond
               (= :table key') (str "TABLE " (name value'))
               (= :user key') (str "USER " (name value'))
               :else ""))

           :else ""))))

(defn- handle-on [on]
  (when on
    (str "ON "
         (cond
           (= :root on) "ROOT"
           (= :ns on) "NS"
           (= :namespace on) "NAMESPACE"
           (= :db on) "DB"
           (= :database on) "DATABASE"

           (map? on)
           (let [[key' value'] (first on)]
             (cond
               (= :table key') (str "TABLE " (name value'))
               (= :user key') (str "USER " (name value'))
               :else ""))

           :else ""))))

(defn format-info-for [{:keys [info-for on]}]
  (->> [(handle-info-for info-for)
        (handle-on on)]
       (filter identity)
       (str/join " ")))
