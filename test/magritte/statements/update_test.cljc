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
; UPDATE user:one UNSET email, address;
;
; -- Update all records which match the condition
; UPDATE city SET population = 9541000 WHERE name = 'London';
;
; -- Update all records with the same content
; UPDATE person CONTENT {
; 	name: 'Tobie',
; 	company: 'SurrealDB',
; 	skills: ['Rust', 'Go', 'JavaScript'],
; };
;
; -- Update a specific record with some content
; UPDATE person:tobie CONTENT {
; 	name: 'Tobie',
; 	company: 'SurrealDB',
; 	skills: ['Rust', 'Go', 'JavaScript'],
; };
;
; -- Update certain fields on all records
; UPDATE person MERGE {
; 	settings: {
; 		marketing: true,
; 	},
; };
;
; -- Update certain fields on a specific record
; UPDATE person:tobie MERGE {
; 	settings: {
; 		marketing: true,
; 	},
; };
;
; -- Patch the JSON response
; UPDATE person:tobie PATCH [
; 	{
; 		"op": "add",
; 		"path": "Engineering",
; 		"value": "true"
; 	}
; ]
;
;
; -- Don't return any result
; UPDATE person SET interests += 'reading' RETURN NONE;
;
; -- Return the changeset diff
; UPDATE person SET interests += 'reading' RETURN DIFF;
;
; -- Return the record before changes were applied
; UPDATE person SET interests += 'reading' RETURN BEFORE;
;
; -- Return the record after changes were applied (the default)
; UPDATE person SET interests += 'reading' RETURN AFTER;
;
; -- Return a specific field only from the updated records
; UPDATE person:tobie SET interests = ['skiing', 'music'] RETURN name, interests;
;
; CREATE person SET name = 'Tobie';
; // A single id works: $tobie will now look something like person:qz8mnqmuzs2w6emdn08q
; LET $tobie = SELECT VALUE id FROM ONLY person WHERE name = 'Tobie' LIMIT 1;
; UPDATE $tobie SET skills += 'breathing';
;
; CREATE person SET name = 'Jaime';
; // An entire record also works:
; // $jaime will look something like { id: person:y5o8pf1hkerlhdzf7jyb, name: 'Jaime' }
; LET $jaime = SELECT * FROM ONLY person WHERE name = 'Jaime' LIMIT 1;
; UPDATE $jaime SET skills += 'breathing';
;
; CREATE person SET name = 'Uther';
; // But a record without an id field will not work
; LET $uther = SELECT name FROM ONLY person WHERE name = 'Uther' LIMIT 1;
; // Error: "Can not execute UPDATE statement using value '{ name: 'Uther' }'"
; UPDATE $uther SET skills += 'breathing';
;
; UPDATE person SET important = true WHERE ->knows->person->(knows WHERE influencer = true) TIMEOUT 5s;
  )

