(ns magritte.statements.for-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [magritte.statements.for :refer [format-for]]))

(deftest format-for-test
  (testing "create a person for everyone in the array"
    (is (= "FOR $name IN ['Tobie', 'Jaime'] { CREATE type::thing('person', $name) CONTENT {name: $name}; };"
           (format-for '(for [name ["Tobie" "Jaime"]]
                          {:create  (type/thing "person" :$name)
                           :content {:name :$name}})))))

; -- Set can_vote to true for every person over 18 years old.
; FOR $person IN (SELECT VALUE id FROM person WHERE age >= 18) {
; 	UPDATE $person SET can_vote = true;
; };

  #_(testing "create a person for everyone from select result"
      (is (= "FOR $person IN (SELECT VALUE id FROM person WHERE age >= 18) { UPDATE $person SET can_vote = true; }"
             (format-for '(for [$person {:select-value :id
                                         :from         :person
                                         :where        (>= :age 18)}]
                            {:update $person
                             :set {:can_vote true}}))))))

