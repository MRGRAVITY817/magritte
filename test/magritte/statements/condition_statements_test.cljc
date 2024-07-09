(ns magritte.statements.condition-statements-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [format-if format-let format-when]]))

(deftest format-if-test
  (testing "simple if statement"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-if '(if (= 9 9) "Nine is indeed nine")))))
  (testing "simple if-else"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE { 'Nine is not nine' };"
           (format-if '(if (= 9 9)
                         "Nine is indeed nine"
                         "Nine is not nine")))))
  (testing "if statement inside let block"
    (is (= "LET $badly_formatted_datetime = '2024-04TT08:08:08Z';\nIF !type::is::datetime($badly_formatted_datetime) { THROW 'Whoops, that is not a real datetime' };"
           (format-let '(let [badly_formatted_datetime "2024-04TT08:08:08Z"]
                          (if (not (type/is-datetime badly_formatted_datetime))
                            (throw "Whoops, that is not a real datetime"))))))))

(deftest format-when-test
  (testing "if statement without else"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-when '(when (= 9 9) "Nine is indeed nine")))))
  (testing "should ignore else"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-when '(when (= 9 9) "Nine is indeed nine" "Nine is not nine"))))))
