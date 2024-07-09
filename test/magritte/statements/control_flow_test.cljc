(ns magritte.statements.control-flow-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.format :refer [format-cond format-if format-let
                                       format-statement format-when]]
   [magritte.utils :as utils]))

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

(defn- format-condp [arg1]
  (let [[fn-name operator operand & branches] arg1]
    (when (and (= fn-name 'condp)
               operator operand)
      (let [else-branch (if (odd? (count branches))
                          (str " ELSE { "
                               (format-statement (last branches) {:surround-with-parens? false})
                               " }")
                          "")
            branches (partition 2 branches)

            branches (->> branches
                          (map-indexed (fn [idx [condition statement]]
                                         (str (cond
                                                (= idx 0) "IF "
                                                :else " ELSE IF ")
                                              (when (not= :else condition)
                                                (utils/list->str `(~operator ~condition ~operand)))
                                              " { "
                                              (format-statement statement {:surround-with-parens? false})
                                              " }"))))]

        (str (str/join "" branches) else-branch ";")))))

(comment
  ;; get elements except the first and last one
  (rest (butlast [1 2 3 4 5])) ; => (2 3 4)
  (condp = 6
    9 "Nine is indeed nine"
    8 "Nine is not nine"
    "Nine is not nine")
  (partition 2 [0 1 3 4 4]))

(deftest test-format-condp
  (testing "simple condp statement"
    (is (= "IF (9 = 9) { 'Nine is indeed nine' } ELSE IF (8 = 9) { 'Nine is not nine' } ELSE { 'Nine is not nine' };"
           (format-condp '(condp = 9
                            9 "Nine is indeed nine"
                            8 "Nine is not nine"
                            "Nine is not nine"))))))
