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
                                      :skills  ["Rust" "Go" "JavaScript"]}}))))
  (testing "create multiple records"
    (is (= "CREATE townsperson, cat, dog SET created_at = time::now(), name = ('Just a ' + meta::tb(id))"
           (format-create {:create [:townsperson :cat :dog]
                           :set    {:created_at '(time/now)
                                    :name       '(+ "Just a " (meta/tb :id))}}))))
  (testing "create only one record"
    (is (= "CREATE ONLY person:tobie SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript']"
           (format-create {:create-only :person:tobie
                           :set         {:name    "Tobie"
                                         :company "SurrealDB"
                                         :skills  ["Rust" "Go" "JavaScript"]}}))))
; CREATE person SET age = 46, username = "john-smith" RETURN NONE;
  (testing "create with return clause"
    (is (= "CREATE person SET age = 46, username = 'john-smith' RETURN NONE"
           (format-create {:create :person
                           :set    {:age      46
                                    :username "john-smith"}
                           :return :none})))
    (is (= "CREATE person SET age = 46, username = 'john-smith' RETURN DIFF"
           (format-create {:create :person
                           :set    {:age      46
                                    :username "john-smith"}
                           :return :diff})))
    (is (= "CREATE person SET age = 46, username = 'john-smith' RETURN BEFORE"
           (format-create {:create :person
                           :set    {:age      46
                                    :username "john-smith"}
                           :return :before})))
    (is (= "CREATE person SET age = 46, username = 'john-smith' RETURN AFTER"
           (format-create {:create :person
                           :set    {:age      46
                                    :username "john-smith"}
                           :return :after})))
    (is (= "CREATE person SET age = 46, username = 'john-smith', interests = ['skiing', 'music'] RETURN interests"
           (format-create {:create :person
                           :set    {:age      46
                                    :username "john-smith"
                                    :interests ["skiing" "music"]}
                           :return [:interests]})))))

