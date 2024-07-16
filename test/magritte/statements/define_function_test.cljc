(ns magritte.statements.define-function-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.define-function :refer [defn? format-defn]]))

(deftest test-defn?
  (testing "is defn"
    (is (defn? '(defn fn/greet [name :string]
                  (+ "Hello, " name "!"))))
    (is (defn? '(defn fn/greet [name :string]
                  (+ "Hello, " name "!") 1 2 3))))
  (testing "is not defn"
    (is (not (defn? '(defn fn/greet [name] ;; missing type hint
                       (+ "Hello, " name "!")))))
    (is (not (defn? '(defn fn/greet (^:string name) ;; arglist is not a vector
                       (+ "Hello, " name "!")))))
    (is (not (defn? '(def fn/greet [^:string name] ;; missing 'n'
                       (+ "Hello, " name "!")))))
    (is (not (defn? '(def "fn/greet" [^:string name] ;; symbol is a string
                       (+ "Hello, " name "!")))))
    (is (not (defn? "I am defn"))) ;; not a list
    (is (not (defn? '()))))) ;; empty list

(deftest test-format-defn
  (testing "Example usage"

    (is (= (str
            "DEFINE FUNCTION fn::greet($name: string) {"
            "RETURN ('Hello, ' + $name + '!');"
            "}")
           (format-defn '(defn fn/greet [name :string]
                           (+ "Hello, " name "!")))))))




