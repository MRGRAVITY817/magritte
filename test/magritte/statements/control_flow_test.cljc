(ns magritte.statements.control-flow-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [magritte.statements.format :refer [format-cond format-if format-let
                                                format-condp format-when]]))

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

(comment
  ;; get elements except the first and last one
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
                           "Nine is not nine"))))))

(comment
  (def expression 9)
  (def clauses [9 "Nine is indeed nine"
                8 "Nine is not nine"
                "Nine is not nine"])

   ; => (condp = 9 9 "Nine is indeed nine" 8 "Nine is not nine" "Nine is not nine"
  )

