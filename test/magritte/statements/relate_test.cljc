(ns magritte.statements.relate-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.relate :refer [format-relate]]))

(deftest test-format-relate
  (testing "relate with content"
    (is (= "RELATE $from->purchases->$to CONTENT {quantity: $after.quantity, total: $after.total, status: 'Pending'}"
           (format-relate '{:relate  :$from->purchases->$to
                            :content {:quantity (:quantity $after)
                                      :total    (:total $after)
                                      :status   "Pending"}})))))
