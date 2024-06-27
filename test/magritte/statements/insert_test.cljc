(ns magritte.statements.insert-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.insert :refer [format-insert]]))

(deftest test-insert-statement
  (testing "insert simple record with content"
    (is (= "INSERT INTO person {name: 'Tobie', company: 'SurrealDB', skills: ['Rust', 'Go', 'JavaScript']}"
           (format-insert {:insert :person
                           :content {:name    "Tobie"
                                     :company "SurrealDB"
                                     :skills  ["Rust" "Go" "JavaScript"]}}))))

  ;; add more tests
  )
