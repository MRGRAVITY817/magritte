(ns magritte.statements.select-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.select :refer [format-select]]
            [magritte.functions.array-functions :refer [array-fn]]))

(deftest format-select-test
  (testing "select all fields from a table"
    (is (= "SELECT * FROM person"
           (format-select {:select [:*]
                           :from   [:person]}))))
  (testing "select specific fields from a table"
    (is (= "SELECT name, address, email FROM person"
           (format-select {:select [:name :address :email]
                           :from   [:person]}))))
  (testing "select all fields from a specific record"
    (is (= "SELECT * FROM person:tobie"
           (format-select {:select [:*]
                           :from   [:person:tobie]}))))
  (testing "select specific fields from a specific record"
    (is (= "SELECT name, address, email FROM person:tobie"
           (format-select {:select [:name :address :email]
                           :from   [:person:tobie]}))))
  (testing "select specific fields with aliases"
    (is (= "SELECT name AS username, address FROM person"
           (format-select {:select [[:name :username] :address]
                           :from   [:person]}))))
  (testing "select just a single record"
    (is (= "SELECT * FROM ONLY person:john"
           (format-select {:select    [:*]
                           :from-only :person:john}))))
  (testing "select the values of a single field from a table"
    (is (= "SELECT VALUE name FROM person"
           (format-select {:select-value :name
                           :from         [:person]}))))
  (testing "select the values of a single field from a specific record"
    (is (= "SELECT VALUE name FROM person:00e1nc508h9f7v63x72O"
           (format-select {:select-value :name
                           :from         [:person:00e1nc508h9f7v63x72O]})))))

(deftest advanced-format-select-test
  (testing "select nested objects/values"
    (is (= "SELECT address.city FROM person"
           (format-select {:select [:address.city]
                           :from   [:person]}))))
  (testing "select all nested array values"
    (is (= "SELECT address.*.coordinates AS coordinates FROM person"
           (format-select {:select [[:address.*.coordinates :coordinates]]
                           :from   [:person]})))
    (is (= "SELECT address.coordinates AS coordinates FROM person"
           (format-select {:select [[:address.coordinates :coordinates]]
                           :from   [:person]}))))
  (testing "select unique values from an array"
    (is (= "SELECT array::distinct(tags) FROM article"
           (format-select {:select [(array-fn :distinct :tags)]
                           :from   [:article]}))))
  (testing  "select unique values from a nested array across an entire table"
    (is (= "SELECT array::group([title, tags]) AS title_tags FROM article GROUP ALL"
           (format-select {:select [[(array-fn :group [:title :tags]) :title_tags]]
                           :from   [:article]
                           :group  :all}))))
  (testing "select mathematical calculations"
    (is (= "SELECT ((celsius * 2) + 30) AS fahrenheit FROM temperature"
           (format-select {:select [['(+ (* :celsius 2) 30) :fahrenheit]]
                           :from   [:temperature]}))))
  (testing "select boolean expressions with an alias"
    (is (= "SELECT (rating >= 4) AS positive FROM review"
           (format-select {:select [['(>= :rating 4) :positive]]
                           :from   [:review]}))))
  (testing "select object structure"
    (is (= "SELECT {weekly: false, monthly: true} AS `marketing settings` FROM user"
           (format-select {:select [[{:weekly false :monthly true} "marketing settings"]]
                           :from   [:user]}))))
  (testing "select one item from an array"
    (is (= "SELECT address.coordinates[0] AS latitude FROM person"
           (format-select {:select [[:address.coordinates [0] :latitude]]
                           :from   [:person]}))))
  (testing "select filtered nested array values"
    (is (= "SELECT address[WHERE (active = true)] FROM person"
           (format-select {:select [[:address [:where '(= :active true)]]]
                           :from   [:person]}))))
  (testing "select a person who has reacted to a post using a celebration"
    (is (= "SELECT * FROM person WHERE ->(reacted_to WHERE (type = 'celebrate'))->post"
           (format-select {:select [:*]
                           :from   [:person]
                           :where  '(-> [:reacted_to [:where (= :type "celebrate")]]
                                        :post)}))))
  (testing "select a remote field from connected out graph edges"
    (is (= "SELECT ->likes->friend.name AS friends FROM person:tobie"
           (format-select {:select [['(-> :likes :friend.name) :friends]]
                           :from   [:person:tobie]})))
    (is (= "SELECT ->likes->friend.name AS friends FROM person:tobie"
           (format-select {:select [[:->likes->friend.name :friends]] ;; equivalent to the above
                           :from   [:person:tobie]}))))
  (testing "select a subquery as a returned field"
    (is (= "SELECT *, (SELECT * FROM events WHERE (type = 'activity') LIMIT 5) AS history FROM user"
           (format-select {:select [:* [^:subquery
                                        {:select [:*]
                                         :from   [:events]
                                         :where  '(= :type "activity")
                                         :limit  5} :history]]
                           :from   [:user]})))))

(deftest format-select-test-using-pararmeters
  (testing "assign select result to let"
    (is (= "SELECT * FROM $history"
           (format-select {:select [:*]
                           :from   [:$history]}))))
; -- Use the parent instance's field in a subquery (predefined variable)
; SELECT *, (SELECT * FROM events WHERE host == $parent.id) AS hosted_events FROM user;
  (testing "use the parent instance's field in a subquery"
    (is (= "SELECT *, (SELECT * FROM events WHERE (host == $parent.id)) AS hosted_events FROM user"
           (format-select {:select [:* [^:subquery
                                        {:select [:*]
                                         :from   [:events]
                                         :where  '(== :host $parent.id)} :hosted_events]]
                           :from   [:user]})))))
