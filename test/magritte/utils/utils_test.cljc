(ns magritte.utils.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.utils :as utils]))

(deftest list->infix-test
  (testing "converts '(+ 1 2) to '(1 + 2)'"
    (is (= "(1 + 2)" (utils/list->infix '(+ 1 2)))))
  (testing "converts '(* 1 2) to '(1 * 2)'"
    (is (= "(1 * 2)" (utils/list->infix '(* 1 2)))))
  (testing "converts '(* (+ 1 2) (- 3 4)) to '((1 + 2) * (3 - 4))'"
    (is (= "((1 + 2) * (3 - 4))" (utils/list->infix '(* (+ 1 2) (- 3 4)))))
    (is (= "((1 + 2) * (3 - 4) * (5 / 6))" (utils/list->infix '(* (+ 1 2) (- 3 4) (/ 5 6))))))
  (testing "with keyword"
    (is (= "((rating + 2) * (3 - 4))" (utils/list->infix '(* (+ :rating 2) (- 3 4)))))))

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
           (utils/range-map->str {:> 2 :<= 5})))))
