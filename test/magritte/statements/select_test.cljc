(ns magritte.statements.select-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.select :refer [format-select]]))

(deftest format-select-test
  (testing "format-select"
    (is (= "SELECT * FROM person;"
           (format-select {:select
                           {:fields [:*]
                            :from [:person]}})))))



