(ns magritte.statements.define-function-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.define-function :refer [defn? format-defn]]))

(deftest test-defn?
  (testing "is defn"
    (is (defn? '(defn fn/greet [^:string name]
                  (+ "Hello, " name "!"))))))

#_(deftest test-format-define-function
    (testing "Example usage"

      (is (= (str
              "DEFINE FUNCTION fn::greet($name: string) {"
              "RETURN 'Hello, ' + $name + '!';"
              "}")
             (format-defn '(defn fn/greet [^:string name]
                             (+ "Hello, " name "!")))))))




