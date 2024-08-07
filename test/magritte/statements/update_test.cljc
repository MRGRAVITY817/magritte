(ns magritte.statements.update-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.update :refer [format-update]]))

(deftest format-update-test
  (testing "update all records in a table"
    (is (= "UPDATE person SET skills += 'breathing'"
           (format-update {:update :person
                           :set    ['(+= :skills "breathing")]}))))
  (testing "update or create a record with a specific numeric id"
    (is (= "UPDATE person:100 SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript']"
           (format-update {:update :person:100
                           :set    [{:name    "Tobie"
                                     :company "SurrealDB"
                                     :skills  ["Rust" "Go" "JavaScript"]}]}))))
  (testing "update or create a record with a specific string id"
    (is (= "UPDATE person:tobie SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript']"
           (format-update {:update :person:tobie
                           :set    [{:name    "Tobie"
                                     :company "SurrealDB"
                                     :skills  ["Rust" "Go" "JavaScript"]}]}))))
  (testing "update a single record"
    (is (= "UPDATE ONLY person:tobie SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript']"
           (format-update {:update-only :person:tobie
                           :set         [{:name    "Tobie"
                                          :company "SurrealDB"
                                          :skills  ["Rust" "Go" "JavaScript"]}]}))))
  (testing "update a document and increment a numeric value"
    (is (= "UPDATE webpage:home SET click_count += 1"
           (format-update {:update :webpage:home
                           :set    ['(+= :click_count 1)]}))))
  (testing "update a document and remove a tag from an array"
    (is (= "UPDATE person:tobie SET interests -= 'Java'"
           (format-update {:update :person:tobie
                           :set    ['(-= :interests "Java")]}))))
  (testing "remove a field by setting it to NONE"
    (is (= "UPDATE webpage:home SET click_count = NONE"
           (format-update {:update :webpage:home
                           :set    [{:click_count :none}]}))))
  (testing "remove fields using UNSET"
    (is (= "UPDATE user:one UNSET email, address"
           (format-update {:update :user:one
                           :unset  [:email :address]}))))
  (testing "update all records which match the condition"
    (is (= "UPDATE city SET population = 9541000 WHERE (name = 'London')"
           (format-update {:update :city
                           :set    [{:population 9541000}]
                           :where  '(= :name "London")}))))
  (testing "update all records with the same content"
    (is (= "UPDATE person CONTENT {name: 'Tobie', company: 'SurrealDB', skills: ['Rust', 'Go', 'JavaScript']}"
           (format-update {:update :person
                           :content {:name    "Tobie"
                                     :company "SurrealDB"
                                     :skills  ["Rust" "Go" "JavaScript"]}}))))
  (testing "update a specific record with some content"
    (is (= "UPDATE person:tobie CONTENT {name: 'Tobie', company: 'SurrealDB', skills: ['Rust', 'Go', 'JavaScript']}"
           (format-update {:update  :person:tobie
                           :content {:name    "Tobie"
                                     :company "SurrealDB"
                                     :skills  ["Rust" "Go" "JavaScript"]}}))))
  (testing "update certain fields on all records"
    (is (= "UPDATE person MERGE {settings: {marketing: true}}"
           (format-update {:update :person
                           :merge  {:settings {:marketing true}}}))))
  (testing "update certain fields on a specific record"
    (is (= "UPDATE person:tobie MERGE {settings: {marketing: true}}"
           (format-update {:update :person:tobie
                           :merge  {:settings {:marketing true}}}))))
  (testing "patch the JSON response"
    (is (= "UPDATE person:tobie PATCH [{\"op\":\"add\",\"path\":\"Engineering\",\"value\":\"true\"}]"
           (format-update {:update :person:tobie
                           :patch  [{:op    "add"
                                     :path  "Engineering"
                                     :value "true"}]}))))
  (testing "update, but don't return the result"
    (is (= "UPDATE person SET interests += 'reading' RETURN NONE"
           (format-update {:update :person
                           :set    ['(+= :interests "reading")]
                           :return :none}))))
  (testing "update and return the changeset diff"
    (is (= "UPDATE person SET interests += 'reading' RETURN DIFF"
           (format-update {:update :person
                           :set    ['(+= :interests "reading")]
                           :return :diff}))))
  (testing "return the record beofore changes were applied"
    (is (= "UPDATE person SET interests += 'reading' RETURN BEFORE"
           (format-update {:update :person
                           :set    ['(+= :interests "reading")]
                           :return :before}))))
  (testing "return the record after changes were applied"
    (is (= "UPDATE person SET interests += 'reading' RETURN AFTER"
           (format-update {:update :person
                           :set    ['(+= :interests "reading")]
                           :return :after}))))
  (testing "update and return specific field(s)"
    (is (= "UPDATE person:tobie SET interests = ['skiing', 'music'] RETURN name, interests"
           (format-update {:update :person:tobie
                           :set    [{:interests ["skiing" "music"]}]
                           :return [:name :interests]}))))
  (testing "update a record which is bound by let"
    (is (= "UPDATE $tobie SET skills += 'breathing'"
           (format-update {:update :$tobie
                           :set    ['(+= :skills "breathing")]}))))
  (testing "update with timeout"
    (is (= "UPDATE person SET important = true WHERE ->knows->person->(knows WHERE (influencer = true)) TIMEOUT 5s"
           (format-update {:update :person
                           :set    [{:important true}]
                           :where  '(|-> :knows :person [:knows [:where (= :influencer true)]])
                           :timeout 5})))))

