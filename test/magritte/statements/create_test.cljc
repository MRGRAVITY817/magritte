(ns magritte.statements.create-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.create :refer [format-create]]))

(deftest test-basic-create
  (testing "format-create"
    (is (= "CREATE person"
           (format-create {:create :person})))))

