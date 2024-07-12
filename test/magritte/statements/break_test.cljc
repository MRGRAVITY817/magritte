(ns magritte.statements.break-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.format :refer [format-break]]))

(deftest test-format-break
  (testing "simple break statement"
    (is (= "BREAK"
           (format-break '(break))))))


