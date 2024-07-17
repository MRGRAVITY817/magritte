(ns magritte.statements.define-function-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [defn? format-defn]]))

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
                           (+ "Hello, " name "!"))))))
  (testing "With let"
    (is (= (str
            "DEFINE FUNCTION fn::relation_exists($in: record, $tb: string, $out: record) {"
            "LET $results = (SELECT VALUE id FROM type::table($tb) WHERE ((in = $in) AND (out = $out)));\n"
            "RETURN (array::len($results) > 0);"
            "}")
           (format-defn '(defn fn/relation_exists [in :record
                                                   tb :string
                                                   out :record]
                           (let [results {:select-value :id
                                          :from         [(type/table tb)]
                                          :where        (and (= :in in) (= :out out))}]
                             (> (array/len results) 0)))))))
  (testing "Multiple statements"
    (is (= (str
            "DEFINE FUNCTION fn::greet($name: string) {"
            "SELECT name, age FROM users WHERE (name = $name);\n"
            "CREATE person:100 CONTENT {name: 'Tobie', company: 'SurrealDB', skills: ['Rust', 'Go', 'JavaScript']};\n"
            "RETURN ('Hello, ' + $name + '!');"
            "}")
           (format-defn '(defn fn/greet [name :string]
                           {:select [:name :age]
                            :from   [:users]
                            :where  (= :name name)}
                           {:create :person:100
                            :content {:name    "Tobie"
                                      :company "SurrealDB"
                                      :skills  ["Rust" "Go" "JavaScript"]}}
                           (+ "Hello, " name "!")))))))




