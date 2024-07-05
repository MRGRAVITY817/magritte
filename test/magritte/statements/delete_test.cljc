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
  (testing "return the record after changes were applied"
    (is (= "DELETE user WHERE (interests CONTAINS 'reading') RETURN AFTER"
           (format-delete {:delete :user
                           :where  '(contains? :interests "reading")
                           :return :after}))))
  (testing "delete all records which match the condition with a timeout"
    (is (= "DELETE person WHERE (influencer = false) TIMEOUT 5s"
           (format-delete {:delete :person
                           :where  '(= :influencer false)
                           :timeout 5}))))
  (testing "delete all records which match the condition with a subquery"
    (is (= "DELETE person:tobie->bought WHERE (out = product:iphone)"
           (format-delete {:delete :person:tobie->bought
                           :where  '(= :out :product:iphone)})))))
