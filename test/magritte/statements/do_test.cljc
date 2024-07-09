(ns magritte.statements.do-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [format-do]]))

(deftest format-do-test
  (testing "do has one statement"
    (is (= "'Nine is indeed nine';\n"
           (format-do '(do "Nine is indeed nine")))))
  (testing "do has multiple statements"
    (is (= "'Nine is indeed nine';\nSELECT * FROM table;\ntime::now();\n"
           (format-do '(do
                         "Nine is indeed nine"
                         {:select [:*]
                          :from   [:table]}
                         (time/now)))))))
