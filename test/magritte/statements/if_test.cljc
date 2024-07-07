(ns magritte.statements.if-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.format :refer [format-if]]))

(deftest format-if-test
;; IF 9 = 9 { RETURN 'Nine is indeed nine' };
  (testing "simple if statement"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-if '(if (= 9 9) "Nine is indeed nine")))))

; LET $badly_formatted_datetime = "2024-04TT08:08:08Z";
; IF !type::is::datetime($badly_formatted_datetime) {
;     THROW "Whoops, that isn't a real datetime"
; };
  )

