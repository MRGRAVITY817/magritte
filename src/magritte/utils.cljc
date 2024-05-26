(ns magritte.utils
  (:require [clojure.string :as str]))

(defn to-str-items [fields]
  (->> fields (map name) (str/join ", ")))
