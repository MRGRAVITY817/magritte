(ns magritte.statements.select-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.select :refer [format-select]]))

(deftest format-select-test
  (testing "format-select"
    (is (= "SELECT * FROM person;"
           (format-select {:select [:*]
                           :from   [:person]})))
    (is (= "SELECT name, address, email FROM person;"
           (format-select {:select [:name :address :email]
                           :from   [:person]})))
    (is (= "SELECT * FROM person:tobie;"
           (format-select {:select [:*]
                           :from   [:person:tobie]})))
    (is (= "SELECT name, address, email FROM person:tobie;"
           (format-select {:select [:name :address :email]
                           :from   [:person:tobie]})))
    (is (= "SELECT name AS username, address FROM person;"
           (format-select {:select [[:name :username] :address]
                           :from   [:person]})))
    (is (= "SELECT * FROM ONLY person:john;"
           (format-select {:select    [:*]
                           :from-only :person:john}))))
    ; -- Select the values of a single field from a table
    ; SELECT VALUE name FROM person;
  (is (= "SELECT VALUE name FROM person;"
         (format-select {:select-value :name
                         :from         [:person]})))
    ; -- Select the values of a single field from a specific record
    ; SELECT VALUE name FROM person:00e1nc508h9f7v63x72O;
  )



