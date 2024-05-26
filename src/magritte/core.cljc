(ns magritte.core)

(defn sum [a b]
  #?(:clj (+ a b)
     :cljs (+ a b)))
