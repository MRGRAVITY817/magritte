(ns magritte.statements.define-function)

(defn defn? [[first' second' third' :as expr]]
  (prn first' second' third' expr)
  (prn (name second'))
  (and (list? expr)
       (= first' 'defn)
       (symbol? second')
       (vector? third')))

(comment
  (defn? '(defn greet [^:string name]
            (+ "Hello, " name "!"))))

(defn format-defn [expr]
  (when (defn? expr)))
