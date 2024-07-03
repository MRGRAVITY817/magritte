(ns magritte.statements.update-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.update :refer [format-update]]))

(deftest format-update-test
; -- Update all records in a table
; -- The skills field is an array. The += operator alone is enough for SurrealDB to infer the type
; UPDATE person SET skills += 'breathing';

  (testing "update all records in a table"
    (is (= "UPDATE person SET skills += 'breathing'"
           (format-update {:update :person
                           :set    ['(+= :skills "breathing")]}))))

; -- Update or create a record with a specific numeric id
; UPDATE person:100 SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript'];
;
; -- Update or create a record with a specific string id
; UPDATE person:tobie SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript'];
;
; -- Update just a single record
; -- Using the ONLY keyword, just an object for the record in question will be returned.
; -- This, instead of an array with a single object.
; UPDATE ONLY person:tobie SET name = 'Tobie', company = 'SurrealDB', skills = ['Rust', 'Go', 'JavaScript'];
;
; -- Update a document and increment a numeric value
; UPDATE webpage:home SET click_count += 1;
;
; -- Update a document and remove a tag from an array
; UPDATE person:tobie SET interests -= 'Java';
;
; -- Remove a field by setting it to NONE or using the UNSET keyword
; UPDATE webpage:home SET click_count = NONE;
;
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

