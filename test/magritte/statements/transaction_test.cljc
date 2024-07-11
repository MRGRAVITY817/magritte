(ns magritte.statements.transaction-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.transaction :refer [format-begin format-cancel]]))

(deftest test-format-transaction-fns
  (testing "begin-transaction"
    (is (= "BEGIN TRANSACTION;"
           (format-begin '(begin-transaction)))))
  (testing "begin"
    (is (= "BEGIN TRANSACTION;"
           (format-begin '(begin)))))
  (testing "cancel-transaction"
    (is (= "CANCEL TRANSACTION;"
           (format-cancel '(cancel-transaction)))))
  (testing "cancel"
    (is (= "CANCEL TRANSACTION;"
           (format-cancel '(cancel)))))
  ;
  )
