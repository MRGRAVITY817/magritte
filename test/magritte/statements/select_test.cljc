(ns magritte.statements.select-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.statements.select :refer [format-select]]))

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
           (format-select {:select ['(array/distinct :tags)]
                           :from   [:article]}))))
  (testing  "select unique values from a nested array across an entire table"
    (is (= "SELECT array::group([title, tags]) AS title_tags FROM article GROUP ALL"
           (format-select {:select [['(array/group [:title :tags]) :title_tags]]
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
           (format-select {:select [[^:object {:weekly false :monthly true} "marketing settings"]]
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
           (format-select {:select [:* [{:select [:*]
                                         :from   [:events]
                                         :where  '(= :type "activity")
                                         :limit  5} :history]]
                           :from   [:user]}))))
  (testing "skip certain fields with OMIT clause"
    (is (= "SELECT * OMIT address, email FROM person"
           (format-select {:select [:*]
                           :omit   [:address :email]
                           :from   [:person]})))))

(deftest format-select-test-using-pararmeters
  (testing "assign select result to let"
    (is (= "SELECT * FROM $history"
           (format-select {:select [:*]
                           :from   [:$history]}))))
  (testing "use the parent instance's field in a subquery"
    (is (= "SELECT *, (SELECT * FROM events WHERE (host == $parent.id)) AS hosted_events FROM user"
           (format-select {:select [:* [{:select [:*]
                                         :from   [:events]
                                         :where  '(== :host $parent.id)} :hosted_events]]
                           :from   [:user]})))))

(deftest format-select-test-record-ranges
  (testing "select all records with IDs between the given range"
    (is (= "SELECT * FROM person:1..1000"
           (format-select {:select [:*]
                           :from   [[:person '(.. 1 1000)]]}))))
  (testing "select all records for a particular location, inclusive"
    (is (= "SELECT * FROM temperature:['London', NONE]..=['London', time::now()]"
           (format-select {:select [:*]
                           :from   [[:temperature '(..= ["London" :none] ["London" (time/now)])]]}))))
  (testing "select all records with IDs less than a maximum value"
    (is (= "SELECT * FROM temperature:..['London', '2022-08-29T08:09:31']"
           (format-select {:select [:*]
                           :from   [[:temperature '(.. nil ["London" "2022-08-29T08:09:31"])]]}))))
  (testing "select all records with IDs greater than a minimum value"
    (is (= "SELECT * FROM temperature:['London', '2022-08-29T08:03:39'].."
           (format-select {:select [:*]
                           :from   [[:temperature '(.. ["London" "2022-08-29T08:03:39"])]]})))))

(deftest test-select-multiple-targets
  (testing "select from two or more tables"
    (is (= "SELECT * FROM person, temperature"
           (format-select {:select [:*]
                           :from   [:person :temperature]})))
    (is (= "SELECT * FROM person, temperature, article"
           (format-select {:select [:*]
                           :from   [:person :temperature :article]}))))
  (testing "select from multiple records"
    (is (= "SELECT * FROM user:tobie, user:jaime, company:surrealdb"
           (format-select {:select [:*]
                           :from   [:user:tobie :user:jaime :company:surrealdb]})))
    (is (= "SELECT * FROM [3648937, 'test', person:lrym5gur8hzws72ux5fa, person:4luro9170uwcv1xrfvby]"
           (format-select {:select [:*]
                           :from   [[3648937 "test" :person:lrym5gur8hzws72ux5fa :person:4luro9170uwcv1xrfvby]]})))
    (is (= "SELECT * FROM {person: person:lrym5gur8hzws72ux5fa, embedded: true}"
           (format-select {:select [:*]
                           :from   [^:object
                                    {:person :person:lrym5gur8hzws72ux5fa :embedded true}]})))
    (is (= "SELECT * FROM (SELECT (age >= 18) AS adult FROM user) WHERE (adult = true)"
           (format-select {:select [:*]
                           :from   [{:select [['(>= :age 18) :adult]]
                                     :from   [:user]}]
                           :where  '(= :adult true)})))))

(deftest test-filter-query-using-where-clause
  (testing "select all records where a field is true"
    (is (= "SELECT * FROM article WHERE (published = true)"
           (format-select {:select [:*]
                           :from   [:article]
                           :where  '(= :published true)}))))
  (testing "conditional filtering based on graph edge properties"
    (is (= "SELECT * FROM profile WHERE (count(->experience->organisation) > 3)"
           (format-select {:select [:*]
                           :from   [:profile]
                           :where  '(> (count (-> :experience :organisation)) 3)}))))
  (testing "conditional filtering based on graph edge properties"
    (is (= "SELECT * FROM person WHERE ->(reaction WHERE (type = 'celebrate'))->post"
           (format-select {:select [:*]
                           :from   [:person]
                           :where  '(-> [:reaction [:where (= :type "celebrate")]]
                                        :post)}))))
  (testing "conditional filtering with boolean logic"
    (is (= "SELECT * FROM user WHERE ((admin AND active) OR (owner = true))"
           (format-select {:select [:*]
                           :from   [:user]
                           :where  '(or (and :admin :active)
                                        (= :owner true))}))))
  (testing "select filtered nested array values"
    (is (= "SELECT address[WHERE (active = true)] FROM person"
           (format-select {:select [[:address [:where '(= :active true)]]]
                           :from   [:person]})))))

(deftest test-select-with-split-clause
  (testing "split the results by each value in an array"
    (is (= "SELECT * FROM user SPLIT emails"
           (format-select {:select [:*]
                           :from   [:user]
                           :split  :emails}))))
  (testing "split the results by each value in a nested array"
    (is (= "SELECT * FROM country SPLIT locations.cities"
           (format-select {:select [:*]
                           :from   [:country]
                           :split  :locations.cities}))))
  (testing "split the results by each value in a subquery"
    (is (= "SELECT * FROM (SELECT * FROM person SPLIT loggedin) WHERE (loggedin > '2023-05-01')"
           (format-select {:select [:*]
                           :from   [{:select [:*]
                                     :from   [:person]
                                     :split  :loggedin}]
                           :where  '(> :loggedin "2023-05-01")})))))

(deftest test-select-with-group-clause
  (testing "group results by a single field"
    (is (= "SELECT country FROM user GROUP BY country"
           (format-select {:select [:country]
                           :from   [:user]
                           :group  [:country]}))))
  (testing "group results by a nested field"
    (is (= "SELECT settings.published FROM article GROUP BY settings.published"
           (format-select {:select [:settings.published]
                           :from   [:article]
                           :group  [:settings.published]}))))
  (testing "group results by multiple fields"
    (is (= "SELECT gender, country, city FROM person GROUP BY gender, country, city"
           (format-select {:select [:gender :country :city]
                           :from   [:person]
                           :group  [:gender :country :city]}))))
  (testing "group results with aggregate functions"
    (is (= "SELECT count() AS total, math::mean(age) AS average_age, gender, country FROM person GROUP BY gender, country"
           (format-select {:select [['(count) :total]
                                    ['(math/mean :age) :average_age]
                                    :gender :country]
                           :from   [:person]
                           :group  [:gender :country]}))))
  (testing "group all records"
    (is (= "SELECT count() AS number_of_records FROM person GROUP ALL"
           (format-select {:select [['(count) :number_of_records]]
                           :from   [:person]
                           :group  :all}))))
  (testing "group unique values from a nested array across an entire table"
    (is (= "SELECT array::group(tags) AS tags FROM article GROUP ALL"
           (format-select {:select [['(array/group :tags) :tags]]
                           :from   [:article]
                           :group  :all})))))

(deftest test-select-with-order-by-clause
  (testing "order records randomly"
    (is (= "SELECT * FROM user ORDER BY rand()"
           (format-select {:select [:*]
                           :from   [:user]
                           :order  ['(rand)]}))))
  (testing "order records descending by a single field"
    (is (= "SELECT * FROM song ORDER BY rating DESC"
           (format-select {:select [:*]
                           :from   [:song]
                           :order  [[:rating :desc]]}))))
  (testing "order records by multiple fields independently"
    (is (= "SELECT * FROM song ORDER BY artist ASC, rating DESC"
           (format-select {:select [:*]
                           :from   [:song]
                           :order  [[:artist :asc] [:rating :desc]]}))))
  (testing "order text fields with unicode collation"
    (is (= "SELECT * FROM article ORDER BY title COLLATE ASC"
           (format-select {:select [:*]
                           :from   [:article]
                           :order  [[:title :collate :asc]]}))))
  (testing "order text fields with numeric values"
    (is (= "SELECT * FROM article ORDER BY title NUMERIC ASC"
           (format-select {:select [:*]
                           :from   [:article]
                           :order  [[:title :numeric :asc]]}))))
  (testing "order text fields with unicode collation and just values"
    (is (= "SELECT * FROM article ORDER BY title COLLATE ASC, rating DESC"
           (format-select {:select [:*]
                           :from   [:article]
                           :order  [[:title :collate :asc] [:rating :desc]]})))))

(deftest test-select-with-limit-clause
  (testing "select only 50 records"
    (is (= "SELECT * FROM person LIMIT 50"
           (format-select {:select [:*]
                           :from   [:person]
                           :limit  50}))))
  (testing "select only 50 records starting at record 50"
    (is (= "SELECT * FROM user LIMIT 50 START 50"
           (format-select {:select [:*]
                           :from   [:user]
                           :limit  [50 :start 50]})))))

(deftest test-select-with-fetch-clause
  (testing "use fetched fields in the select statement"
    (is (= "SELECT *, artist.email FROM review FETCH artist"
           (format-select {:select [:* :artist.email]
                           :from   [:review]
                           :fetch  :artist}))))
; -- Select all the article information 
; -- only if the author's age (from the author table) is under 30.
; SELECT * FROM article WHERE author.age < 30 FETCH author;
  (testing "use fetched fields in the where clause"
    (is (= "SELECT * FROM article WHERE (author.age < 30) FETCH author"
           (format-select {:select [:*]
                           :from   [:article]
                           :where  '(< :author.age 30)
                           :fetch  :author})))))

(deftest test-select-with-timeout-clause
; -- Cancel this conditional filtering based on graph edge properties
; -- if it's not finished within 5 seconds
; SELECT * FROM person WHERE ->knows->person->(knows WHERE influencer = true) TIMEOUT 5s;
  (testing "select with timeout, cancel the query if it takes longer than 5 seconds"
    (is (= "SELECT * FROM person WHERE ->knows->person->(knows WHERE (influencer = true)) TIMEOUT 5s"
           (format-select {:select [:*]
                           :from   [:person]
                           :where  '(-> :knows :person [:knows [:where (= :influencer true)]])
                           :timeout "5s"}))))
  (testing "simple timeout"
    (is (= "SELECT * FROM person TIMEOUT 5s"
           (format-select {:select  [:*]
                           :from    [:person]
                           :timeout 5})))))

(deftest test-select-with-parallel-clause
  (testing "select with parallel clause"
    (is (= "SELECT ->purchased->product<-purchased<-person->purchased->product FROM person:tobie PARALLEL"
           (format-select {:select   [:->purchased->product<-purchased<-person->purchased->product]
                           :from     [:person:tobie]
                           :parallel true})))))

(deftest test-select-with-explain-clause
  (testing "select with explain clause"
    (is (= "SELECT * FROM person EXPLAIN"
           (format-select {:select  [:*]
                           :from    [:person]
                           :explain true})))))

(deftest test-select-with-with-clause
  (testing "select with index clause"
    (is (= "SELECT * FROM person WITH INDEX ft_age, ft_name"
           (format-select {:select [:*]
                           :from   [:person]
                           :with   [:ft_age :ft_name]}))))
  (testing "select with noindex clause"
    (is (= "SELECT * FROM person WITH NOINDEX"
           (format-select {:select [:*]
                           :from   [:person]
                           :with   :noindex})))))

