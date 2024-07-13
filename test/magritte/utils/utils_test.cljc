(ns magritte.utils.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.utils :as utils]))

(deftest list->str-test
  (testing "mathematical expression"
    (is (= "(1 + 2)"
           (utils/list->str '(+ 1 2)))))
  (testing "graph expression"
    (is (= "->person->user->name"
           (utils/list->str '(|-> :person :user :name))))
    (is (= "->person->(user WHERE (name = 'charlie'))->address"
           (utils/list->str '(|-> :person [:user [:where (= name "charlie")]] :address)))))
  (testing "db functions"
    (is (= "time::now()"
           (utils/list->str '(time/now)))))
  (testing "property access with keyword fn"
    (is (= "user.name"
           (utils/list->str '(:name user)))))
  (testing "property access with `get`"
    (is (= "user.name"
           (utils/list->str '(get user :name)))))
  (testing "property access with `get-in`"
    (is (= "user.company.address"
           (utils/list->str '(get-in user [:company :address])))))
  #_(testing "property access with `->`"
      (is (= "user.company.address"
             (utils/list->str '(-> user :company :address))))))

(deftest list->infix-test
  (testing "converts '(+ 1 2) to '(1 + 2)'"
    (is (= "(1 + 2)" (utils/list->infix '(+ 1 2)))))
  (testing "converts '(* 1 2) to '(1 * 2)'"
    (is (= "(1 * 2)" (utils/list->infix '(* 1 2)))))
  (testing "converts '(* (+ 1 2) (- 3 4)) to '((1 + 2) * (3 - 4))'"
    (is (= "((1 + 2) * (3 - 4))" (utils/list->infix '(* (+ 1 2) (- 3 4)))))
    (is (= "((1 + 2) * (3 - 4) * (5 / 6))" (utils/list->infix '(* (+ 1 2) (- 3 4) (/ 5 6))))))
  (testing "with keyword"
    (is (= "((rating + 2) * (3 - 4))" (utils/list->infix '(* (+ :rating 2) (- 3 4))))))
  (testing "complex"
    (is (= "(count(->experience->organisation) > 3)"
           (utils/list->infix '(> (count (|-> :experience :organisation)) 3)))))
  (testing "OR"
    (is (= "((rating > 3) OR (rating < 5))"
           (utils/list->infix '(or (> :rating 3) (< :rating 5))))))
  (testing "AND"
    (is (= "((rating > 3) AND (rating < 5))"
           (utils/list->infix '(and (> :rating 3) (< :rating 5)))))))

(deftest test-symbol->db-fn-name
  (testing "converts 'time/now to time::now()"
    (is (= "time::now" (utils/symbol->db-fn-name 'time/now))))
  (testing "converts 'count to count()"
    (is (= "count" (utils/symbol->db-fn-name 'count))))
  (testing "converts 'rand to rand()"
    (is (= "rand" (utils/symbol->db-fn-name 'rand)))))

(deftest list->db-fn-test
  (testing "time::now()"
    (is (= "time::now()" (utils/list->db-fn '(time/now)))))
  (testing "time::floor(date, interval)"
    (is (= "time::floor('2021-11-01T08:30:17+00:00', 1w)"
           (utils/list->db-fn '(time/floor "2021-11-01T08:30:17+00:00" :1w)))))
  (testing "array::append(list, item)"
    (is (= "array::append([1, 2, 3], 4)"
           (utils/list->db-fn '(array/append [1 2 3] 4)))))
  (testing "array::boolean::and(list, list)"
    (is (= "array::boolean::and(['true', 'false', 1, 1], ['true', 'true', 0, 'true'])"
           (utils/list->db-fn '(array/boolean-and ["true" "false" 1 1] ["true" "true" 0 "true"])))))
  (testing "count(->person->user->name)"
    (is (= "count(->person->user->name)"
           (utils/list->db-fn '(count (|-> :person :user :name))))))
  (testing "converts 'rand to rand()"
    (is (= "rand()" (utils/list->db-fn '(rand))))))

(deftest test-is-range
  (is (utils/is-range? '(.. 1 1000)))
  (is (utils/is-range? '(..= 1 1000)))
  (is (utils/is-range? '(.. nil 1)))
  (is (utils/is-range? '(.. 1))))

(deftest test-list->range
  (is (= "1..1000"
         (utils/list->range '(.. 1 1000))))
  (is (= "['London', NONE]..=['London', time::now()]"
         (utils/list->range '(..= ["London" :none] ["London" (time/now)]))))
  (is (= "..['London', '2022-08-29T08:09:31']"
         (utils/list->range '(.. nil ["London" "2022-08-29T08:09:31"]))))
  (is (= "['London', '2022-08-29T08:03:39'].."
         (utils/list->range '(.. ["London" "2022-08-29T08:03:39"])))))

(deftest map->str-test
  (testing "converts {:id \"hello\"} to {id: 'hello'}"
    (is (= "{id: 'hello'}" (utils/map->str {:id "hello"})))
    (is (= "{id: 'hello', name: 'world'}"
           (utils/map->str {:id "hello" :name "world"})))
    (is (= "{rating: (rating + 2), name: 'world'}"
           (utils/map->str {:rating '(+ :rating 2) :name "world"})))))

(deftest graph->str-test
  (testing "unnested simple graph"
    (is (= "->person->user->name"
           (utils/graph->str '(-> :person :user :name)))))
  (testing "simple graph, with nested elements"
    (is (= "->person->(user WHERE (name = 'charlie'))->address"
           (utils/graph->str '(-> :person
                                  [:user [:where (= name "charlie")]]
                                  :address))))))

(deftest range-map->str-test
  (testing "greater than 2, less than 5"
    (is (= "2..5"
           (utils/range-map->str {:> 2 :< 5}))))
  (testing "greater than 2"
    (is (= "2.."
           (utils/range-map->str {:> 2}))))
  (testing "less than 5"
    (is (= "..5"
           (utils/range-map->str {:< 5}))))
  (testing "greater than or equal to 2, less than or equal to 5"
    (is (= "2=..=5"
           (utils/range-map->str {:>= 2 :<= 5}))))
  (testing "greater than 2, less than or equal to 5"
    (is (= "2..=5"
           (utils/range-map->str {:> 2 :<= 5}))))
  #_(testing "range items are vectors"
      (is (= "['London', NONE]..=['London', time::now()]"
             (utils/range-map->str {:>  ["London" :none]
                                    :<= ["London" '(time/now)]})))))
