(ns magritte.statements.select-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.select :refer [format-select]]))

(deftest format-select-test
  (testing "select all fields from a table"
    (is (= "SELECT * FROM person;"
           (format-select {:select [:*]
                           :from   [:person]}))))
  (testing "select specific fields from a table"
    (is (= "SELECT name, address, email FROM person;"
           (format-select {:select [:name :address :email]
                           :from   [:person]}))))
  (testing "select all fields from a specific record"
    (is (= "SELECT * FROM person:tobie;"
           (format-select {:select [:*]
                           :from   [:person:tobie]}))))
  (testing "select specific fields from a specific record"
    (is (= "SELECT name, address, email FROM person:tobie;"
           (format-select {:select [:name :address :email]
                           :from   [:person:tobie]}))))
  (testing "select specific fields with aliases"
    (is (= "SELECT name AS username, address FROM person;"
           (format-select {:select [[:name :username] :address]
                           :from   [:person]}))))
  (testing "select just a single record"
    (is (= "SELECT * FROM ONLY person:john;"
           (format-select {:select    [:*]
                           :from-only :person:john}))))
  (testing "select the values of a single field from a table"
    (is (= "SELECT VALUE name FROM person;"
           (format-select {:select-value :name
                           :from         [:person]}))))
  (testing "select the values of a single field from a specific record"
    (is (= "SELECT VALUE name FROM person:00e1nc508h9f7v63x72O;"
           (format-select {:select-value :name
                           :from         [:person:00e1nc508h9f7v63x72O]})))))

(deftest advanced-format-select-test
  (testing "select nested objects/values"
    (is (= "SELECT address.city FROM person;"
           (format-select {:select [:address.city]
                           :from   [:person]}))))
  (testing "select all nested array values"
    (is (= "SELECT address.*.coordinates AS coordinates FROM person;"
           (format-select {:select [[:address.*.coordinates :coordinates]]
                           :from   [:person]})))
    ;; equivalent to 
    (is (= "SELECT address.coordinates AS coordinates FROM person;"
           (format-select {:select [[:address.coordinates :coordinates]]
                           :from   [:person]}))))
; -- Select one item from an array
; SELECT address.coordinates[0] AS latitude FROM person;
  (testing "select one item from an array"
    (is (= "SELECT address.coordinates[0] AS latitude FROM person;"
           (format-select {:select [[{:array :address.coordinates
                                      :index 0} :latitude]]
                           :from   [:person]}))))
;
; -- Select unique values from an array
; SELECT array::distinct(tags) FROM article;
  ; (testing "select unique values from an array"
  ;   (is (= "SELECT array::distinct(tags) FROM article;"
  ;          (format-select {:select [{:array :^distint
  ;                                    :fields [:tags]}]
  ;                          :from   [:article]}))))
; -- Select unique values from a nested array across an entire table
; SELECT array::group(tags) AS tags FROM article GROUP ALL;
;
; -- Use mathematical calculations in a select expression
; SELECT ( ( celsius * 2 ) + 30 ) AS fahrenheit FROM temperature;
;
; -- Return boolean expressions with an alias
; SELECT rating >= 4 as positive FROM review;
;
; -- Select manually generated object structure
; SELECT { weekly: false, monthly: true } AS `marketing settings` FROM user;
;
; -- Select filtered nested array values
; SELECT address[WHERE active = true] FROM person;
;
; -- Select a person who has reacted to a post using a celebration
; -- You can see the graph as: person->(reacted_to WHERE type='celebrate')->post
; SELECT * FROM person WHERE ->(reacted_to WHERE type='celebrate')->post;
;
; -- Select a remote field from connected out graph edges
; SELECT ->likes->friend.name AS friends FROM person:tobie;
;
; -- Use the result of a subquery as a returned field
; SELECT *, (SELECT * FROM events WHERE type = 'activity' LIMIT 5) AS history FROM user;
  )
