(ns magritte.statements.define-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.define :refer [format-define]]))

(deftest test-format-define-analyzer
  (testing "define analyzer"
    (is (= "DEFINE ANALYZER example_blank TOKENIZERS blank"
           (format-define {:define     :analyzer
                           :name       :example_blank
                           :tokenizers [:blank]})))))

(deftest test-format-define-database
  (testing "define database"
    (is (= "DEFINE DATABASE users"
           (format-define {:define :database
                           :name   :users}))))
  (testing "define database if not exists"
    (is (= "DEFINE DATABASE IF NOT EXISTS users"
           (format-define {:define [:database :if-not-exists]
                           :name   :users}))))
  (testing "changefeed 3d"
    (is (= "DEFINE DATABASE users CHANGEFEED 3d"
           (format-define {:define     :database
                           :name       :users
                           :changefeed :3d})))))


