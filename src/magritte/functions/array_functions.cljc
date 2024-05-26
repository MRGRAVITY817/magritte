(ns magritte.functions.array-functions
  (:require [clojure.string :as str]
            [magritte.utils :as utils]))

; array::add()	Adds an item to an array if it doesn't exist
; array::all()	Checks whether all array values are truthy
; array::any()	Checks whether any array value is truthy
; array::at()	Returns value for X index, or in reverse for a negative index
; array::append()	Appends an item to the end of an array
; array::boolean_and()	Perform the AND bitwise operations on two arrays
; array::boolean_or()	Perform the OR bitwise operations on two arrays
; array::boolean_xor()	Perform the XOR bitwise operations on two arrays
; array::boolean_not()	Perform the NOT bitwise operations on an array
; array::combine()	Combines all values from two arrays together
; array::complement()	Returns the complement of two arrays
; array::clump()	Returns the original array split into multiple arrays of X size
; array::concat()	Returns the merged values from two arrays
; array::difference()	Returns the difference between two arrays
; array::distinct()	Returns the unique items in an array
; array::find_index()	Returns the index of the first occurrence of X value
; array::filter_index()	Find the indexes of all occurrences of all matching X value
; array::first()	Returns the first item in an array
; array::flatten()	Flattens multiple arrays into a single array
; array::group()	Flattens and returns the unique items in an array
; array::insert()	Inserts an item at the end of an array, or in a specific position, supports negative index
; array::intersect()	Returns the values which intersect two arrays
; array::join()	Returns concatenated value of an array with a string in between.
; array::last()	Returns the last item in an array
; array::len()	Returns the length of an array
; array::logical_and()	Performs the AND logical operations on two arrays
; array::logical_or()	Performs the OR logical operations on two arrays
; array::logical_xor()	Performs the XOR logical operations on two arrays
; array::max()	Returns the maximum item in an array
; array::matches()	Returns an array of booleans
; array::min()	Returns the minimum item in an array
; array::pop()	Returns the last item from an array
; array::prepend()	Prepends an item to the beginning of an array
; array::push()	Appends an item to the end of an array
; array::remove()	Removes an item at a specific position from an array, supports negative index
; array::reverse()	Reverses the sorting order of an array
; array::sort()	Sorts the values in an array in ascending or descending order
; array::slice()	Returns a slice of an array
; array::sort::asc()	Sorts the values in an array in ascending order
; array::sort::desc()	Sorts the values in an array in descending order
; array::transpose()	Performs 2d array transposition on two arrays
; array::union() Returns the unique merged values from two arrays

(defn- args-valid? [validators args]
  (if (= (count validators) (count args))
    (every? identity (map (fn [v a] (v a)) validators args))
    false))

(comment
  (args-valid? [vector? vector?] [["one" "two"] ["three"]])
  (args-valid? [vector? vector?] [["one" "two"] "three"]))

(def ^:private array-functions
  {:add [vector? any?]
   :all [vector?]
   :any [vector?]
   :at  [vector? #(and (integer? %) (not (neg? %)))]
   :append [vector? any?]
   :boolean_and [vector? vector?]
   :boolean_or [vector? vector?]
   :boolean_xor [vector? vector?]
   :boolean_not [vector?]
   :combine [vector? vector?]
   :complement [vector? vector?]
   :clump [vector? integer?]
   :concat [vector? vector?]
   :difference [vector? vector?]
   :distinct [vector?]
   :find_index [vector? any?]
   :filter_index [vector? any?]
   :first [vector?]
   :flatten [vector?]
   :group [vector?]
   :insert [vector? any? #(or (integer? %) (neg? %))]
   :intersect [vector? vector?]
   :join [vector? string?]
   :last [vector?]
   :len [vector?]
   :logical_and [vector? vector?]
   :logical_or [vector? vector?]
   :logical_xor [vector? vector?]
   :max [vector?]
   :matches [vector? any?]
   :min [vector?]
   :pop [vector?]
   :prepend [vector? any?]
   :push [vector? any?]
   :remove [vector? #(or (integer? %) (neg? %))]
   :reverse [vector?]
   :sort [vector?]
   :slice [vector? integer? integer?]
   :sort_asc [vector?]
   :sort_desc [vector?]
   :transpose [vector? vector?]
   :union [vector? vector?]})

(defn array-fn [function & args]
  (let [validator (get array-functions function)]
    (if validator
      (if (args-valid? validator args)
        (str "array::" (name function) "(" (str/join ", " (map utils/to-valid-str args)) ")")
        (throw (ex-info (str "Invalid arguments for array function: " function) {})))
      (throw (ex-info (str "Unknown array function: " function) {})))))

(comment
  (array-fn :add ["one" "two"] "three")
  (array-fn :add ["one" "two"] "three" "four")
  (array-fn :add ["one" "two"] 3)
  ;;
  )


