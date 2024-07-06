(ns magritte.statements.for-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [format-for]]))

(deftest format-for-test
  (testing "create a person for everyone in the array"
    (is (= "FOR $name IN ['Tobie', 'Jaime'] { CREATE type::thing('person', $name) CONTENT {name: $name}; };"
           (format-for '(for [name ["Tobie" "Jaime"]]
                          {:create  (type/thing "person" name)
                           :content {:name name}})))))

  (testing "create a person for everyone from select result"
    (is (= "FOR $person IN (SELECT VALUE id FROM person WHERE (age >= 18)) { UPDATE $person SET can_vote = true; };"
           (format-for '(for [person {:select-value :id
                                      :from         [:person]
                                      :where        (>= :age 18)}]
                          {:update person
                           :set    [{:can_vote true}]})))))
  (testing "nested for loop"
    (is (= "FOR $odd-num IN [1, 3, 5, 7, 9] { FOR $even-num IN [2, 4, 6, 8, 10] { CREATE type::thing('number', $odd-num) CONTENT {value: ($odd-num * $even-num)}; }; };"
           (format-for '(for [odd-num  [1 3 5 7 9]
                              even-num [2 4 6 8 10]]
                          {:create  (type/thing "number" odd-num)
                           :content {:value (* odd-num even-num)}})))))
  (testing "nested for loop with multiple statements"
    (is (= "FOR $odd-num IN [1, 3, 5, 7, 9] { FOR $even-num IN [2, 4, 6, 8, 10] { CREATE type::thing('number', $odd-num) CONTENT {value: ($odd-num * $even-num)};\nUPDATE person SET age = $odd-num WHERE (name = 'Tobie'); }; };"
           (format-for '(for [odd-num  [1 3 5 7 9]
                              even-num [2 4 6 8 10]]
                          {:create  (type/thing "number" odd-num)
                           :content {:value (* odd-num even-num)}}
                          {:update :person
                           :set    [{:age odd-num}]
                           :where  (= :name "Tobie")})))))
  (testing "can bind let value"
    (is (= "FOR $odd-num IN [1, 3, 5, 7, 9] { FOR $even-num IN [2, 4, 6, 8, 10] { LET $age = ($odd-num * $even-num);\nLET $person = (SELECT * FROM person WHERE (age = $age));\nCREATE type::thing('number', $odd-num) CONTENT {value: ($odd-num * $even-num), person: $person, odd-num: $odd-num, even-num: $even-num}; }; };"
           (format-for '(for [odd-num  [1 3 5 7 9]
                              even-num [2 4 6 8 10]
                              :let     [age    (* odd-num even-num)
                                        person {:select [:*]
                                                :from   [:person]
                                                :where  (= :age age)}]]
                          {:create  (type/thing "number" odd-num)
                           :content {:value    (* odd-num even-num)
                                     :person   person
                                     :odd-num  odd-num
                                     :even-num even-num}})))))

  (testing "can put let inside the main statements"
    (is (= "FOR $odd-num IN [1, 3, 5, 7, 9] { FOR $even-num IN [2, 4, 6, 8, 10] { LET $age = ($odd-num * $even-num);\nLET $person = (SELECT * FROM person WHERE (age = $age));\nCREATE type::thing('number', $odd-num) CONTENT {value: ($odd-num * $even-num), person: $person, odd-num: $odd-num, even-num: $even-num}; }; };"
           (format-for '(for [odd-num  [1 3 5 7 9]
                              even-num [2 4 6 8 10]
                              :let     [age (* odd-num even-num)]]
                          (let [person {:select [:*]
                                        :from   [:person]
                                        :where  (= :age age)}]
                            {:create  (type/thing "number" odd-num)
                             :content {:value    (* odd-num even-num)
                                       :person   person
                                       :odd-num  odd-num
                                       :even-num even-num}}))))))

;
  )


