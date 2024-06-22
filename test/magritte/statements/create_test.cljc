(ns magritte.statements.create-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.create :refer [format-create]]))

(deftest test-basic-create
  (testing "create simple table record"
    (is (= "CREATE person"
           (format-create {:create :person})))
    (is (= "CREATE person:100"
           (format-create {:create :person:100}))))
  (testing "create with set clause"
    (is (= "CREATE person:100 SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript']"
           (format-create {:create :person:100
                           :set    {:name    "Tobie"
                                    :company "SurrealDB"
                                    :skills  ["Rust" "Go" "JavaScript"]}}))))
  (testing "create with content clause"
    (is (= "CREATE person:100 CONTENT {name: 'Tobie', company: 'SurrealDB', skills: ['Rust', 'Go', 'JavaScript'],}"
           (format-create {:create   :person:100
                           :content  {:name    "Tobie"
                                      :company "SurrealDB"
                                      :skills  ["Rust" "Go" "JavaScript"]}})))))

