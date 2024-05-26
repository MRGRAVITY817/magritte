(ns magritte.functions.array-functions-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.functions.array-functions :refer [array-fn]]))

(deftest array-functions-tests
  (testing "array::add"
    (is (= "array::add([\"one\", \"two\"], \"three\")"
           (array-fn :add ["one" "two"] "three")))))

