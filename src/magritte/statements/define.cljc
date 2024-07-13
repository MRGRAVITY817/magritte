(ns magritte.statements.define
  (:require
   [clojure.string :as str]))

(def ^:private define-types
  {:database "DATABASE"})

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

(defn format-define-database
  "Formats a define database expression."
  [{:keys [define name]}]
  (->> ["DEFINE"
        (handle-define define)
        (handle-name name)]
       (filter identity)
       (str/join " ")
       (str/trim)))
