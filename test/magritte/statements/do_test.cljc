(ns magritte.statements.do-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [format-do]]))

(deftest format-do-test
  (testing "do has one statement"
    (is (= "'Nine is indeed nine';\n"
           (format-do '(do "Nine is indeed nine"))))))
