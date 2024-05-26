(ns magritte.statements.select-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.select :refer [format-select]]))

(deftest format-select-test
  (testing "format-select"
    (is (= "SELECT * FROM person;"
           (format-select {:select
                           {:fields [:*]
                            :from [:person]}})))
    (is (= "SELECT name, address, email FROM person;"
           (format-select {:select
                           {:fields [:name :address :email]
                            :from [:person]}})))
    (is (= "SELECT * FROM person:tobie;"
           (format-select {:select
                           {:fields [:*]
                            :from [:person:tobie]}})))
    (is (= "SELECT name, address, email FROM person:tobie;"
           (format-select {:select
                           {:fields [:name :address :email]
                            :from [:person:tobie]}})))
    (is (= "SELECT name AS username, address FROM person;"
           (format-select {:select
                           {:fields [[:name :username] :address]
                            :from [:person]}})))
    (is (= "SELECT * FROM ONLY person:john;"
           (format-select {:select
                           {:fields [:*]
                            :from-only :person:john}})))))



