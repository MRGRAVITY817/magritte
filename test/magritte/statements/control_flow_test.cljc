(ns magritte.statements.control-flow-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [format-break format-cond format-condp
                                       format-for format-if format-let
                                       format-when]]))

(deftest test-format-if
  (testing "simple if statement"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-if '(if (= 9 9) "Nine is indeed nine")))))
  (testing "simple if-else"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE { 'Nine is not nine' };"
           (format-if '(if (= 9 9)
                         "Nine is indeed nine"
                         "Nine is not nine")))))
  (testing "if statement inside let block"
    (is (= "LET $badly_formatted_datetime = '2024-04TT08:08:08Z';\nIF !type::is::datetime($badly_formatted_datetime) { THROW 'Whoops, that is not a real datetime' };"
           (format-let '(let [badly_formatted_datetime "2024-04TT08:08:08Z"]
                          (if (not (type/is-datetime badly_formatted_datetime))
                            (throw "Whoops, that is not a real datetime")))))))
  (testing "if statement with do block"
    (is (= "IF (9 = 9) { 'Nine is indeed nine';\nSELECT * FROM table;\ntime::now();\n } ELSE { 'Nine is not nine' };"
           (format-if '(if (= 9 9)
                         (do "Nine is indeed nine"
                             {:select [:*]
                              :from   [:table]}
                             (time/now))
                         "Nine is not nine"))))))

(deftest test-format-when
  (testing "if statement without else"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-when '(when (= 9 9) "Nine is indeed nine")))))
  (testing "should ignore else"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-when '(when (= 9 9) "Nine is indeed nine" "Nine is not nine")))))
  (testing "when inside let block"
    (is (= "LET $badly_formatted_datetime = '2024-04TT08:08:08Z';\nIF !type::is::datetime($badly_formatted_datetime) { THROW 'Whoops, that is not a real datetime' };"
           (format-let '(let [badly_formatted_datetime "2024-04TT08:08:08Z"]
                          (when (not (type/is-datetime badly_formatted_datetime))
                            (throw "Whoops, that is not a real datetime")))))))
  (testing "when statement with do block"
    (is (= "IF (9 = 9) { 'Nine is indeed nine';\nSELECT * FROM table;\ntime::now();\n };"
           (format-when '(when (= 9 9)
                           (do "Nine is indeed nine"
                               {:select [:*]
                                :from   [:table]}
                               (time/now))))))))

(deftest test-format-cond
  (testing "simple cond statement"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE IF (9 = 8) { 'Nine is not nine' } ELSE { 'Nine is not nine' };"
           (format-cond '(cond
                           (= 9 9) "Nine is indeed nine"
                           (= 9 8) "Nine is not nine"
                           :else "Nine is not nine")))))
  (testing "without :else"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE IF (9 = 8) { 'Nine is not nine' };"
           (format-cond '(cond
                           (= 9 9) "Nine is indeed nine"
                           (= 9 8) "Nine is not nine")))))
  (testing "only one condition"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' };"
           (format-cond '(cond
                           (= 9 9) "Nine is indeed nine")))))
  (testing "cond inside let block"
    (is (= "LET $badly_formatted_datetime = '2024-04TT08:08:08Z';\nIF !type::is::datetime($badly_formatted_datetime) { 'Nine is indeed nine' } ELSE IF (9 = 8) { 'Nine is not nine' } ELSE { 'Nine is not nine' };"
           (format-let '(let [badly_formatted_datetime "2024-04TT08:08:08Z"]
                          (cond
                            (not (type/is-datetime badly_formatted_datetime)) "Nine is indeed nine"
                            (= 9 8) "Nine is not nine"
                            :else "Nine is not nine"))))))
  (testing "cond statement with do block"
    (is (= "IF (9 = 9) { 'Nine is indeed nine';\nSELECT * FROM table;\ntime::now();\n } ELSE IF (9 = 8) { 'Nine is not nine' };"
           (format-cond '(cond
                           (= 9 9) (do "Nine is indeed nine"
                                       {:select [:*]
                                        :from   [:table]}
                                       (time/now))
                           (= 9 8) "Nine is not nine"))))))

(deftest test-format-condp
  (testing "simple condp statement"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE IF (8 = 9) { 'Nine is not nine' } ELSE { 'Nine is not nine' };"
           (format-condp '(condp = 9
                            9 "Nine is indeed nine"
                            8 "Nine is not nine"
                            "Nine is not nine"))))))

(defn- format-case [[fn-name expression & clauses]]
  (when (= fn-name 'case)
    (format-condp `(~'condp ~'= ~expression
                            ~@clauses))))

(deftest test-format-case
  (testing "simple case statement"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE IF (8 = 9) { 'Nine is not nine' } ELSE { 'Nine is not nine' };"
           (format-case '(case 9
                           9 "Nine is indeed nine"
                           8 "Nine is not nine"
                           "Nine is not nine")))))
  (testing "more branches"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE IF (8 = 9) { 'Nine is not nine' } ELSE IF ('Nine' = 9) { 'Nine is string' } ELSE { 'Nine is not nine' };"
           (format-case '(case 9
                           9 "Nine is indeed nine"
                           8 "Nine is not nine"
                           "Nine" "Nine is string"
                           "Nine is not nine"))))))

(deftest test-format-for
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
  (testing "use if, else if, and break"
    (is (= "FOR $odd-num IN [1, 3, 5, 7, 9] { FOR $even-num IN [2, 4, 6, 8, 10] { LET $age = ($odd-num * $even-num);\nLET $person = (SELECT * FROM person WHERE (age = $age));\nIF type::is::datetime($age) { CREATE type::thing('number', $odd-num) CONTENT {value: ($odd-num * $even-num), person: $person, odd-num: $odd-num, even-num: $even-num} } ELSE { CREATE type::thing('number', $odd-num) CONTENT {value: ($odd-num * $even-num), person: $person, odd-num: $odd-num, even-num: $even-num};\nBREAK;\n }; }; };"
           (format-for '(for [odd-num  [1 3 5 7 9]
                              even-num [2 4 6 8 10]
                              :let     [age (* odd-num even-num)]]
                          (let [person {:select [:*]
                                        :from   [:person]
                                        :where  (= :age age)}]
                            (if (type/is-datetime age)
                              {:create  (type/thing "number" odd-num)
                               :content {:value    (* odd-num even-num)
                                         :person   person
                                         :odd-num  odd-num
                                         :even-num even-num}}
                              (do
                                {:create  (type/thing "number" odd-num)
                                 :content {:value    (* odd-num even-num)
                                           :person   person
                                           :odd-num  odd-num
                                           :even-num even-num}}
                                (break)))))))))

;
  )

(deftest test-format-break
  (testing "simple break statement"
    (is (= "BREAK"
           (format-break '(break))))))

(comment
  (def expression 9)
  (def clauses [9 "Nine is indeed nine"
                8 "Nine is not nine"
                "Nine is not nine"])

  (rest (butlast [1 2 3 4 5])) ; => (2 3 4)
  (case 9
    9 "Nine is indeed nine"
    8 "Nine is not nine"
    "Nine is not nine")
  (condp = 6
    9 "Nine is indeed nine"
    8 "Nine is not nine"
    "Nine is not nine")

  (condp clojure.string/includes? "a"
    "abc" :>> #(str "a is in abc: " %)
    "def" :>> "a is not in def"
    #(str "alphabet: " %))

  (partition 2 [0 1 3 4 4]))




