(ns magritte.statements.define-test
  (:require
   [clojure.test :refer [deftest is testing] :as test]
   [magritte.statements.define :refer [format-define]]))

(deftest test-format-define-analyzer
  (testing "with one tokenizer"
    (is (= "DEFINE ANALYZER example_blank TOKENIZERS blank"
           (format-define {:define     :analyzer
                           :name       :example_blank
                           :tokenizers [:blank]}))))
  (testing "with multiple tokenizers"
    (is (= "DEFINE ANALYZER example_blank TOKENIZERS blank,camel"
           (format-define {:define     :analyzer
                           :name       :example_blank
                           :tokenizers [:blank :camel]}))))
  (testing "with a filter"
    (is (= "DEFINE ANALYZER example_ascii TOKENIZERS class FILTERS ascii"
           (format-define {:define     :analyzer
                           :name       :example_ascii
                           :tokenizers [:class]
                           :filters    [:ascii]}))))
  (testing "with multiple filters"
    (is (= "DEFINE ANALYZER example_ascii TOKENIZERS class FILTERS ascii,ngram(1, 3)"
           (format-define '{:define     :analyzer
                            :name       :example_ascii
                            :tokenizers [:class]
                            :filters    [:ascii (ngram 1 3)]}))))
  (testing "if not exists"
    (is (= "DEFINE ANALYZER IF NOT EXISTS example_ascii TOKENIZERS class FILTERS ascii,ngram(1, 3)"
           (format-define '{:define     [:analyzer :if-not-exists]
                            :name       :example_ascii
                            :tokenizers [:class]
                            :filters    [:ascii (ngram 1 3)]})))))

(deftest test-format-define-database
  (testing "define database"
    (is (= "DEFINE DATABASE users"
           (format-define {:define :database
                           :name   :users}))))
  (testing "define database if not exists"
    (is (= "DEFINE DATABASE IF NOT EXISTS users"
           (format-define {:define [:database :if-not-exists]
                           :name   :users}))))
  (testing "changefeed 3d"
    (is (= "DEFINE DATABASE users CHANGEFEED 3d"
           (format-define {:define     :database
                           :name       :users
                           :changefeed :3d})))))

(deftest test-format-define-event
  (testing "define event when user email changed"
    (is (= (str "DEFINE EVENT email ON TABLE user WHEN ($before.email != $after.email) THEN ("
                "CREATE event SET user = $value.id, time = time::now(), value = $after.email, action = 'email_changed'"
                ")")
           (format-define '{:define :event
                            :name   :email
                            :on     [:table :user]
                            :when   (!= (:email $before) (:email $after))
                            :then   {:create :event
                                     :set    {:user   (:id $value)
                                              :time   (time/now)
                                              :value  (:email $after)
                                              :action "email_changed"}}}))))
  (testing "create a relation between a customer and a product whenever a purchase is made"
    (is (= (str
            "DEFINE EVENT purchase ON TABLE purchase WHEN ($before == NONE) THEN {"
            "LET $from = (SELECT * FROM customer WHERE (id == $after.customer));\n"
            "LET $to = (SELECT * FROM product WHERE (id == $after.product));\n"

            "RELATE $from->purchases->$to CONTENT {"
            "quantity: $after.quantity, "
            "total: $after.total, "
            "status: 'Pending'"
            "};"
            "}")
           (format-define '{:define :event
                            :name   :purchase
                            :on     [:table :purchase]
                            :when   (== $before :none)
                            :then   (let [from {:select  [*]
                                                :from    [:customer]
                                                :where   (== :id (:customer $after))}
                                          to   {:select [*]
                                                :from   [:product]
                                                :where  (== :id (:product $after))}]
                                      {:relate  (>-> from :purchases to)
                                       :content {:quantity (:quantity $after)
                                                 :total    (:total $after)
                                                 :status   "Pending"}})}))))
  (testing "combine multiple events"
    (is (= (str
            "DEFINE EVENT user_event ON TABLE user "
            "WHEN (($event = 'CREATE') OR ($event = 'UPDATE') OR ($event = 'DELETE')) THEN ("
            "CREATE log SET "
            "table = 'user', "
            "event = $event, "
            "happened_at = time::now()"
            ")")
           (format-define '{:define :event
                            :name   :user_event
                            :on     [:table :user]
                            :when   (or (= $event "CREATE")
                                        (= $event "UPDATE")
                                        (= $event "DELETE"))
                            :then   {:create :log
                                     :set    {:table       "user"
                                              :event       $event
                                              :happened_at (time/now)}}}))))
  (testing "if not exists"
    (is (= "DEFINE EVENT IF NOT EXISTS email ON user WHEN ($before.email != $after.email)"
           (format-define '{:define [:event :if-not-exists]
                            :name   :email
                            :on     :user
                            :when   (!= (:email $before) (:email $after))})))))

(comment
  (test/run-tests)
  :rcf)
