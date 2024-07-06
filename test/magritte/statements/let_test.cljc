(ns magritte.statements.let-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.let :refer [format-let]]))

(deftest format-let-test
  (testing "define the parameter"
    (is (= "LET $name = 'tobie';"
           (format-let '(let [name "tobie"])))))
  (testing "define and use the parameter"
    (is (= "LET $name = 'tobie';\nCREATE person SET name = $name;"
           (format-let '(let [name "tobie"]
                          {:create :person
                           :set    {:name name}})))))

; -- Define the parameter
; LET $adults = (SELECT * FROM person WHERE age > 18);

; -- Use the parameter
; UPDATE $adults SET adult = true;
  )
