(ns magritte.statements.info-for
  (:require [clojure.string :as str]))

(defn- handle-info-for [info-for])

(defn- handle-on [on])

(defn format-info-for [{:keys [info-for on]}]
  (->> [(handle-info-for info-for)
        (handle-on on)]
       (filter identity)
       (str/join " ")))
