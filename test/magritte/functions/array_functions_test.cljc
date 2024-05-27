(ns magritte.functions.array-functions-test
  (:require [clojure.test :refer [deftest is testing]]
            [magritte.functions.array-functions :refer [array-fn]]))

(deftest array-functions-tests
  (testing "array::add"
    (is (= "array::add(['one', 'two'], 'three')"
           (array-fn :add ["one" "two"] "three"))))
  (testing "array::all"
    (is (= "array::all([1, 2, 3, NONE, 'SurrealDB', 5])"
           (array-fn :all [1 2 3 :none "SurrealDB" 5]))))
  (testing "array::any"
    (is (= "array::any([1, 2, 3, NONE, 'SurrealDB', 5])"
           (array-fn :any [1 2 3 :none "SurrealDB" 5]))))
  (testing "array::at"
    (is (= "array::at(['s', 'u', 'r', 'r', 'e', 'a', 'l'], 2)"
           (array-fn :at ["s" "u" "r" "r" "e" "a" "l"] 2)))
    (is (= "array::at(['s', 'u', 'r', 'r', 'e', 'a', 'l'], -3)"
           (array-fn :at ["s" "u" "r" "r" "e" "a" "l"] -3))))
  (testing "array::append"
    (is (= "array::append([1, 2, 3, 4], 5)"
           (array-fn :append [1 2 3 4] 5))))
  (testing "array::boolean_and"
    (is (= "array::boolean_and(['true', 'false', 1, 1], ['true', 'true', 0, 'true'])"
           (array-fn :boolean_and ["true" "false" 1 1] ["true" "true" 0 "true"])))
    (is (= "array::boolean_and([true, true], [false])"
           (array-fn :boolean_and [true true] [false]))))
  (testing "array::boolean_or"
    (is (= "array::boolean_or([false, true, false, true], [false, false, true, true])"
           (array-fn :boolean_or [false true false true] [false false true true]))))
  (testing "array::boolean_xor"
    (is (= "array::boolean_xor([false, true, false, true], [false, false, true, true])"
           (array-fn :boolean_xor [false true false true] [false false true true]))))
  (testing "array::boolean_not"
    (is (= "array::boolean_not([false, true, 0, 1])"
           (array-fn :boolean_not [false true 0 1]))))
  (testing "array::combine"
    (is (= "array::combine([1, 2], [2, 3])"
           (array-fn :combine [1 2] [2 3]))))
  (testing "array::complement"
    (is (= "array::complement([1, 2, 3, 4], [3, 4, 5, 6])"
           (array-fn :complement [1 2 3 4] [3 4 5 6]))))
  (testing "array::concat"
    (is (= "array::concat([1, 2, 3, 4], [3, 4, 5, 6])"
           (array-fn :concat [1 2 3 4] [3 4 5 6]))))
  (testing "array::clump"
    (is (= "array::clump([1, 2, 3, 4, 5], 2)"
           (array-fn :clump [1 2 3 4 5] 2))))
  (testing "array::difference"
    (is (= "array::difference([1, 2, 3, 4], [3, 4, 5, 6])"
           (array-fn :difference [1 2 3 4] [3 4 5 6]))))
  (testing "array::distinct"
    (is (= "array::distinct([1, 2, 1, 3, 3, 4])"
           (array-fn :distinct [1 2 1 3 3 4]))))
  (testing "array::flatten"
    (is (= "array::flatten([[1, 2], [3, 4], 'SurrealDB', [5, 6, [7, 8]]])"
           (array-fn :flatten [[1 2] [3 4] "SurrealDB" [5 6 [7 8]]]))))
  (testing "array::find_index"
    (is (= "array::find_index(['a', 'b', 'c', 'b', 'a'], 'b')"
           (array-fn :find_index ["a" "b" "c" "b" "a"] "b"))))
  (testing "array::filter_index"
    (is (= "array::filter_index(['a', 'b', 'c', 'b', 'a'], 'b')"
           (array-fn :filter_index ["a" "b" "c" "b" "a"] "b"))))
  (testing "array::first"
    (is (= "array::first(['s', 'u', 'r', 'r', 'e', 'a', 'l'])"
           (array-fn :first ["s" "u" "r" "r" "e" "a" "l"]))))
  (testing "array::group"
    (is (= "array::group([1, 2, 3, 4, [3, 5, 6], [2, 4, 5, 6], 7, 8, 8, 9])"
           (array-fn :group [1 2 3 4 [3 5 6] [2 4 5 6] 7 8 8 9]))))
  (testing "array::insert"
    (is (= "array::insert([1, 2, 3, 4], 5, 2)"
           (array-fn :insert [1 2 3 4] 5 2))))
  (testing "array::intersect"
    (is (= "array::intersect([1, 2, 3, 4], [3, 4, 5, 6])"
           (array-fn :intersect [1 2 3 4] [3 4 5 6]))))
  (testing "array::join"
    (is (= "array::join(['again', 'again', 'again'], ' and ')"
           (array-fn :join ["again" "again" "again"] " and "))))
  (testing "array::last"
    (is (= "array::last(['s', 'u', 'r', 'r', 'e', 'a', 'l'])"
           (array-fn :last ["s" "u" "r" "r" "e" "a" "l"]))))
  (testing "array::len"
    (is (= "array::len([1, 2, 1, null, 'something', 3, 3, 4, 0])"
           (array-fn :len [1 2 1 nil "something" 3 3 4 0]))))
;   (testing "array::logical_and"
;     (is (= "array::logical_and([true, false, true, false], [true, true, false, false])"
;            (array-fn :logical_and [true false true false] [true true false false]))))
;   (testing "array::logical_or"
;     (is (= "array::logical_or([true, false, true, false], [true, true, false, false])"
;            (array-fn :logical_or [true false true false] [true true false false]))))
;   (testing "array::logical_xor"
;     (is (= "array::logical_xor([true, false, true, false], [true, true, false, false])"
;            (array-fn :logical_xor [true false true false] [true true false false]))))
;   (testing "array::max"
;     (is (= "array::max([0, 1, 2])"
;            (array-fn :max [0 1 2]))))
;   (testing "array::matches"
;     (is (= "array::matches([0, 1, 2], 1)"
;            (array-fn :matches [0 1 2] 1))
;         (is (= "array::matches([{id: 'ohno:0'}, {id: 'ohno:1'}], {id: 'ohno:1'})"
;                (array-fn :matches [{:id "ohno:0"} {:id "ohno:1"}] {:id "ohno:1"})))))
;   (testing "array::min"
;     (is (= "array::min([0, 1, 2])"
;            (array-fn :min [0 1 2]))))
;   (testing "array::pop"
;     (is (= "array::pop([ 1, 2, 3, 4 ])"
;            (array-fn :pop [1 2 3 4]))))
;   (testing "array::prepend"
;     (is (= "array::prepend([1,2,3,4], 5)"
;            (array-fn :prepend [1 2 3 4] 5))))
;   (testing "array::push"
;     (is (= "array::push([1,2,3,4], 5)"
;            (array-fn :push [1 2 3 4] 5))))
;   (testing "array::remove"
;     (is (= "array::remove([1,2,3,4,5], 2)"
;            (array-fn :remove [1 2 3 4 5] 2))
;         (is (= "array::remove([1,2,3,4,5], -2)"
;                (array-fn :remove [1 2 3 4 5] -2)))))
;   (testing "array::reverse"
;     (is (= "array::reverse([ 1, 2, 3, 4, 5 ])"
;            (array-fn :reverse [1 2 3 4 5]))))
;   (testing "array::sort"
;     (is (= "array::sort([ 1, 2, 1, null, 'something', 3, 3, 4, 0 ])"
;            (array-fn :sort [1 2 1 nil "something" 3 3 4 0])))
;     (is (= "array::sort([1,2,1,null,'something',3,3,4,0], false)"
;            (array-fn :sort [1 2 1 nil "something" 3 3 4 0] false)))
;     (is (= "array::sort([1,2,1,null,'something',3,3,4,0], 'asc')"
;            (array-fn :sort [1 2 1 nil "something" 3 3 4 0] "asc")))
;     (is (= "array::sort([1,2,1,null,'something',3,3,4,0], 'desc')"
;            (array-fn :sort [1 2 1 nil "something" 3 3 4 0] "desc"))))
;   (testing "array::slice"
;     (is (= "array::slice([ 1, 2, 3, 4, 5 ], 1, 2)"
;            (array-fn :slice [1 2 3 4 5] 1 2)))
;     (is (= "array::slice([ 1, 2, 3, 4, 5 ], 1, -1)"
;            (array-fn :slice [1 2 3 4 5] 1 -1)))
;     (is (= "array::slice([ 1, 2, 3, 4, 5 ], 2)"
;            (array-fn :slice [1 2 3 4 5] 2)))
;     (is (= "array::slice([ 1, 2, 3, 4, 5 ], -2)"
;            (array-fn :slice [1 2 3 4 5] -2)))
;     (is (= "array::slice([ 1, 2, 3, 4, 5 ], -3, 2)"
;            (array-fn :slice [1 2 3 4 5] -3 2))))
;   (testing "array::sort::asc"
;     (is (= "array::sort::asc([ 1, 2, 1, null, 'something', 3, 3, 4, 0 ])"
;            (array-fn :sort::asc [1 2 1 nil "something" 3 3 4 0]))))
;   (testing "array::sort::desc"
;     (is (= "array::sort::desc([ 1, 2, 1, null, 'something', 3, 3, 4, 0 ])"
;            (array-fn :sort::desc [1 2 1 nil "something" 3 3 4 0]))))
;   (testing "array::transpose"
;     (is (= "array::transpose([[0, 1], [2, 3]])"
;            (array-fn :transpose [[0 1] [2 3]]))))
;   (testing "array::union"
;     (is (= "array::union([1,2,1,6], [1,3,4,5,6])"
;            (array-fn :union [1 2 1 6] [1 3 4 5 6]))))
;
  )

