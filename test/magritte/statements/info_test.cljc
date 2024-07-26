(ns magritte.statements.info-test
  (:require
   [clojure.test :refer [deftest is]]
   [magritte.statements.info-for :refer [format-info-for]]))

(deftest format-info-test
  (is (= "INFO FOR ROOT"
         (format-info-for '{:info-for :root})))
  (is (= "INFO FOR NS"
         (format-info-for '{:info-for :ns})))
  (is (= "INFO FOR NAMESPACE"
         (format-info-for '{:info-for :namespace})))
  (is (= "INFO FOR DB"
         (format-info-for '{:info-for :db})))
  (is (= "INFO FOR DATABASE"
         (format-info-for '{:info-for :database})))
  (is (= "INFO FOR TABLE user"
         (format-info-for '{:info-for {:table :user}})))
  (is (= "INFO FOR USER root ON ROOT"
         (format-info-for '{:info-for {:user :root}
                            :on       :root})))

;
  )

