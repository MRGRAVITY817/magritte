(ns magritte.statements.let-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.let :refer [format-let]]))

(deftest format-let-test
; -- Define the parameter
; LET $name = "tobie";
  (testing "define the parameter"
    (is (= "LET $name = 'tobie';"
           (format-let '(let [name "tobie"])))))

; -- Use the parameter
; CREATE person SET name = $name;

; -- Define the parameter
; LET $adults = (SELECT * FROM person WHERE age > 18);

; -- Use the parameter
; UPDATE $adults SET adult = true;
  )
