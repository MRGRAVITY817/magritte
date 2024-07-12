(ns magritte.statements.define-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.define :refer [format-define-database]]))

(deftest test-format-define-database
  (testing "define database"
    (is (= "DEFINE DATABASE users;"
           (format-define-database '(defdb :users)))))
  (testing "define database if not exists"
    (is (= "DEFINE DATABASE IF NOT EXISTS users;"
           (format-define-database '(defdb :users :if-not-exists))))))

