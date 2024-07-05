(ns magritte.statements.delete-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.delete :refer [format-delete]]))

(deftest format-delete-test
  (testing "delete all records in a table"
    (is (= "DELETE person"
           (format-delete {:delete :person}))))
;
; -- Delete a record with a specific numeric id
; DELETE person:100;
;
; -- Delete a record with a specific string id
; DELETE person:tobie;
;
; -- Delete just a single record
; -- Using the ONLY keyword, just an object for the record in question will be returned.
; -- This, instead of an array with a single object.
; DELETE ONLY person:tobie;
; -- Update all records which match the condition
; DELETE city WHERE name = 'London';
; -- Don't return any result (the default)
; DELETE user WHERE age < 18 RETURN NONE;
;
; -- Return the changeset diff
; DELETE user WHERE interests CONTAINS 'reading' RETURN DIFF;
;
; -- Return the record before changes were applied
; DELETE user WHERE interests CONTAINS 'reading' RETURN BEFORE;
;
; -- Return the record after changes were applied
; DELETE user WHERE interests CONTAINS 'reading' RETURN AFTER;
; DELETE person WHERE ->knows->person->(knows WHERE influencer = false) TIMEOUT 5s;
; DELETE person:tobie->bought WHERE out=product:iphone;
  )
