(ns magritte.functions.array-functions
  (:require [clojure.string :as str]
            [magritte.utils :as utils]))

(defn- args-valid? [validators args]
  (if (>= (count validators) (count args))
    (every? identity (map (fn [v a] (v a)) validators args))
    false))

(comment
  (args-valid? [vector? vector?] [["one" "two"] ["three"]])
  (args-valid? [vector? vector?] [["one" "two"] "three"])
  (every? identity (map (fn [v a] (v a)) [vector? #(or (boolean? %) (nil?  %))] [["hello"]])))

(def ^:private array-functions
  {:add [vector? any?] ; Adds an item to an array if it doesn't exist
   :all [vector?] ; Checks whether all array values are truthy
   :any [vector?] ; Checks whether any array value is truthy
   :at  [vector? integer?] ; Returns value for X index, or in reverse for a negative index
   :append [vector? any?] ; Appends an item to the end of an array
   :boolean-and [vector? vector?] ; Perform the AND bitwise operations on two arrays
   :boolean-or [vector? vector?] ; Perform the OR bitwise operations on two arrays
   :boolean-xor [vector? vector?] ; Perform the XOR bitwise operations on two arrays
   :boolean-not [vector?] ; Perform the NOT bitwise operations on an array
   :combine [vector? vector?] ; Combines all values from two arrays together
   :complement [vector? vector?] ; Returns the complement of two arrays
   :clump [vector? integer?] ; Returns the original array split into multiple arrays of X size
   :concat [vector? vector?] ; Returns the merged values from two arrays
   :difference [vector? vector?] ; Returns the difference between two arrays
   :distinct [#(or (vector? %) (keyword? %))] ; Returns the unique items in an array
   :find-index [vector? any?] ; Returns the index of the first occurrence of X value
   :filter-index [vector? any?] ; Find the indexes of all occurrences of all matching X value
   :first [vector?] ; Returns the first item in an array
   :flatten [vector?] ; Flattens multiple arrays into a single array
   :group [vector?] ; Flattens and returns the unique items in an array
   :insert [vector? any? #(or (integer? %) (neg? %))] ; Inserts an item at the end of an array, or in a specific position, supports negative index
   :intersect [vector? vector?] ; Returns the values which intersect two arrays
   :join [vector? string?] ; Returns concatenated value of an array with a string in between.
   :last [vector?] ; Returns the last item in an array
   :len [vector?] ; Returns the length of an array
   :logical-and [vector? vector?] ; Performs the AND logical operations on two arrays
   :logical-or [vector? vector?] ; Performs the OR logical operations on two arrays
   :logical-xor [vector? vector?] ; Performs the XOR logical operations on two arrays
   :max [vector?] ; Returns the maximum item in an array
   :matches [vector? any?] ; Returns an array of booleans
   :min [vector?] ; Returns the minimum item in an array
   :pop [vector?] ; Returns the last item from an array
   :prepend [vector? any?] ; Prepends an item to the beginning of an array
   :push [vector? any?] ; Appends an item to the end of an array
   :remove [vector? #(or (integer? %) (neg? %))] ; Removes an item at a specific position from an array, supports negative index
   :reverse [vector?] ; Reverses the sorting order of an array
   :sort [vector? #(or (string? %) (boolean? %) (nil? %))] ; Sorts the values in an array in ascending or descending order
   :slice [vector? integer? integer?] ; Returns a slice of an array
   [:sort :asc] [vector?] ; Sorts the values in an array in ascending order
   [:sort :desc] [vector?] ; Sorts the values in an array in descending order
   :transpose [vector? vector?] ; Performs 2d array transposition on two arrays
   :union [vector? vector?]}) ; Returns the unique merged values from two arrays

(defn- new-function [function]
  (cond
    (keyword? function) (utils/kebab->snake_name function)
    (vector? function) (str/join "::" (map utils/kebab->snake_name function))))

(defn array-fn [function & args]
  (let [validator (get array-functions function)]
    (if validator
      (if (args-valid? validator args)
        (str "array::" (new-function function) "(" (str/join ", " (map utils/to-valid-str args)) ")")
        (throw (ex-info (str "Invalid arguments for array function: " function) {})))
      (throw (ex-info (str "Unknown array function: " function) {})))))

(comment
  (array-fn :add ["one" "two"] "three")
  (array-fn :add ["one" "two"] "three" "four")
  (array-fn :add ["one" "two"] 3)
  ;;
  )


