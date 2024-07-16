(ns magritte.statements.define-function)

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
       (every? (fn [[value type]] (and (symbol? value) (keyword? type)))
               (partition 2 third'))))

(comment
  (defn? '(defn greet [name :string]
            (+ "Hello, " name "!"))))

(defn format-defn [expr]
  (when (defn? expr)))
