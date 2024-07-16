(ns magritte.statements.define-function
  (:require
   [clojure.string :as str]
   [magritte.statements.common :as common]
   [magritte.utils :as utils]))

(defn defn?
  "Returns true if the given expression is a defn form.
   The defn form must have the following structure:

   ```
   (defn name [arg1 :type1 arg2 :type2 ...]
     body)
   ```
   "
  [[first' second' third' :as expr]]
  (and (list? expr)
       (= first' 'defn)
       (symbol? second')
       (vector? third')
       (even? (count third'))
       (every? (fn [[arg type]] (and (symbol? arg) (keyword? type)))
               (partition 2 third'))))

(comment
  (defn? '(defn greet [name :string]
            (+ "Hello, " name "!"))))

(defn format-defn [expr]
  (when (defn? expr)
    (let [[_ fn-name args body] expr
          arg-str (->> args
                       (partition 2)
                       (map (fn [[arg type]]
                              (str "$" arg ": " (name type))))
                       (str/join ", "))
          params  (->> args
                       (partition 2)
                       (map first)
                       (set))
          body-str (utils/->query-str (common/replace-symbols body params))]
      (str "DEFINE FUNCTION fn::" (name fn-name) "(" arg-str ") {"
           "RETURN " body-str ";"
           "}"))))

