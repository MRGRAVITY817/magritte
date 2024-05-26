(ns magritte.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.core :refer [sum]]))

(deftest sum-test
  (testing "sum of 1 and 2 is 3"
    (is (= 3 (sum 1 2)))))




