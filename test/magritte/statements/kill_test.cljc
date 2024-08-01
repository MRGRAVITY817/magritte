(ns magritte.statements.kill-test
  (:require
   [clojure.test :refer [deftest is testing]]))

(defn- format-kill [arg1]
  (str "KILL " arg1))

(deftest kill-statement-test
  (testing "kill UUID"
    (is (= "KILL u\"0189d6e3-8eac-703a-9a48-d9faa78b44b9\""
           (format-kill '{:kill (uuid "0189d6e3-8eac-703a-9a48-d9faa78b44b9")})))))
