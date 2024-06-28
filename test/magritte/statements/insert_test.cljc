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
  (testing "insert a single record with VALUES"
    (is (= "INSERT INTO company (name, founded) VALUES ('SurrealDB', '2021-09-10')"
           (format-insert {:insert [:company [:name :founded]]
                           :values ["SurrealDB" "2021-09-10"]}))))

; -- Insert multiple records with VALUES
; INSERT INTO company (name, founded) VALUES ('Acme Inc.', '1967-05-03'), ('Apple Inc.', '1976-04-01');
  (testing "insert multiple records with VALUES"
    (is (= "INSERT INTO company (name, founded) VALUES ('Acme Inc.', '1967-05-03'), ('Apple Inc.', '1976-04-01')"
           (format-insert {:insert [:company [:name :founded]]
                           :values [["Acme Inc." "1967-05-03"]
                                    ["Apple Inc." "1976-04-01"]]}))))

; -- When a record already exists, update the record by adding a tag
; INSERT INTO product (name, url) VALUES ('Salesforce', 'salesforce.com') ON DUPLICATE KEY UPDATE tags += 'crm';

; -- Refer input value within ON DUPLICATE KEY UPDATE
; INSERT INTO city (id, population, at_year) VALUES ("Calgary", 1665000, 2024)
; ON DUPLICATE KEY UPDATE
; 	population = $input.population,
; 	at_year = $input.at_year;

; -- Insert a record with a subquery
; INSERT INTO recordings_san_francisco (SELECT * FROM temperature WHERE city = 'San Francisco');

; -- bulk insert
; INSERT INTO person [
;    {id: "person:jaime", name: "Jaime", surname: "Morgan Hitchcock"},
;    {id: "person:tobie", name: "Tobie", surname: "Morgan Hitchcock"},
; ]

  ;; add more tests
  )
