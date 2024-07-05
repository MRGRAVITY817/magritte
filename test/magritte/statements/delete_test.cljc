(ns magritte.statements.delete-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.delete :refer [format-delete]]))

(deftest format-delete-test
  (testing "delete all records in a table"
    (is (= "DELETE person"
           (format-delete {:delete :person}))))
  (testing "delete a record with a specific numeric id"
    (is (= "DELETE person:100"
           (format-delete {:delete :person:100}))))
  (testing "delete a record with a specific string id"
    (is (= "DELETE person:tobie"
           (format-delete {:delete :person:tobie}))))
  (testing "delete a single record"
    (is (= "DELETE ONLY person:tobie"
           (format-delete {:delete-only :person:tobie}))))
  (testing "delete all records which match the condition"
    (is (= "DELETE city WHERE (name = 'London')"
           (format-delete {:delete :city
                           :where  '(= :name "London")}))))
  (testing "don't return any result (the default)"
    (is (= "DELETE user WHERE (age < 18) RETURN NONE"
           (format-delete {:delete :user
                           :where  '(< :age 18)
                           :return :none}))))
  (testing "return the record before changes were applied"
    (is (= "DELETE user WHERE (interests CONTAINS 'reading') RETURN BEFORE"
           (format-delete {:delete :user
                           :where  '(contains? :interests "reading")
                           :return :before}))))
;
; -- Return the record before changes were applied
; DELETE user WHERE interests CONTAINS 'reading' RETURN BEFORE;
;
; -- Return the record after changes were applied
; DELETE user WHERE interests CONTAINS 'reading' RETURN AFTER;
; DELETE person WHERE ->knows->person->(knows WHERE influencer = false) TIMEOUT 5s;
; DELETE person:tobie->bought WHERE out=product:iphone;
  )
