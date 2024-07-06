(ns magritte.statements.let-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [format-let]]))

(deftest format-let-test
  (testing "define the parameter"
    (is (= "LET $name = 'tobie';"
           (format-let '(let [name "tobie"])))))
  (testing "define and use the parameter"
    (is (= "LET $name = 'tobie';\nCREATE person SET name = $name;"
           (format-let '(let [name "tobie"]
                          {:create :person
                           :set    {:name name}})))))
  (testing "define with subquery and use it"
    (is (= "LET $adults = (SELECT * FROM person WHERE (age > 18));\nUPDATE $adults SET adult = true;"
           (format-let '(let [adults {:select [:*]
                                      :from   [:person]
                                      :where  (> :age 18)}]
                          {:update adults
                           :set    [{:adult true}]})))))
  (testing "define several parameters"
    (is (= "LET $name = 'tobie';\nLET $age = (SELECT age FROM person WHERE (name = 'tobie'));\nLET $adult = true;\nCREATE person SET name = $name, age = $age, adult = $adult;"
           (format-let '(let [name  "tobie"
                              age   {:select [:age]
                                     :from   [:person]
                                     :where  (= :name "tobie")}
                              adult true]
                          {:create :person
                           :set    {:name name
                                    :age  age
                                    :adult adult}})))))
  (testing "define several parameters and use them in several statements"
    (is (= "LET $name = 'tobie';\nLET $age = (SELECT age FROM person WHERE (name = 'tobie'));\nLET $adult = true;\nCREATE person SET name = $name, age = $age, adult = $adult RETURN NONE;\nUPDATE person SET age = $age WHERE (name = $name);"
           (format-let '(let [name  "tobie"
                              age   {:select [:age]
                                     :from   [:person]
                                     :where  (= :name "tobie")}
                              adult true]
                          {:create :person
                           :set    {:name  name
                                    :age   age
                                    :adult adult}
                           :return :none}
                          {:update :person
                           :set    [{:age age}]
                           :where  (= :name name)})))))
  (testing "shadowing the binding"
    (is (= "LET $name = 'tobio';\nLET $name = 'annie';\nCREATE person SET name = $name;"
           (format-let '(let [name "tobio"
                              name "annie"]
                          {:create :person
                           :set    {:name name}})))))
  (testing "use bound value in binding"
    (is (= "LET $name = 'tobie';\nLET $age = (SELECT age FROM person WHERE (name = $name));\nCREATE person SET name = $name, age = $age;"
           (format-let '(let [name  "tobie"
                              age   {:select [:age]
                                     :from   [:person]
                                     :where  (= :name name)}]
                          {:create :person
                           :set    {:name name
                                    :age  age}})))))

  (testing "for loop inside let"
    (is (= "LET $name = 'tobie';\nLET $age = (SELECT age FROM person WHERE (name = $name));\nFOR $person IN (SELECT * FROM person WHERE (age = $age)) { CREATE person SET name = $name, age = $age; };"
           (format-let '(let [name  "tobie"
                              age   {:select [:age]
                                     :from   [:person]
                                     :where  (= :name name)}]
                          (for [person {:select [:*]
                                        :from   [:person]
                                        :where  (= :age age)}]
                            {:create :person
                             :set    {:name name
                                      :age  age}}))))))

;;
  )
