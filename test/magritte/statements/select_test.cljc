(ns magritte.statements.select-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.select :refer [format-select]]))

(deftest format-select-test
  (testing "select all fields from a table"
    (is (= "SELECT * FROM person;"
           (format-select {:select [:*]
                           :from   [:person]}))))
  (testing "select specific fields from a table"
    (is (= "SELECT name, address, email FROM person;"
           (format-select {:select [:name :address :email]
                           :from   [:person]}))))
  (testing "select all fields from a specific record"
    (is (= "SELECT * FROM person:tobie;"
           (format-select {:select [:*]
                           :from   [:person:tobie]}))))
  (testing "select specific fields from a specific record"
    (is (= "SELECT name, address, email FROM person:tobie;"
           (format-select {:select [:name :address :email]
                           :from   [:person:tobie]}))))
  (testing "select specific fields with aliases"
    (is (= "SELECT name AS username, address FROM person;"
           (format-select {:select [[:name :username] :address]
                           :from   [:person]}))))
  (testing "select just a single record"
    (is (= "SELECT * FROM ONLY person:john;"
           (format-select {:select    [:*]
                           :from-only :person:john}))))
  (testing "select the values of a single field from a table"
    (is (= "SELECT VALUE name FROM person;"
           (format-select {:select-value :name
                           :from         [:person]}))))
  (testing "select the values of a single field from a specific record"
    (is (= "SELECT VALUE name FROM person:00e1nc508h9f7v63x72O;"
           (format-select {:select-value :name
                           :from         [:person:00e1nc508h9f7v63x72O]})))))



