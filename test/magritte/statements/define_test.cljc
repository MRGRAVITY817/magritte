(ns magritte.statements.define-test
  (:require
   [clojure.test :refer [deftest is testing] :as test]
   [magritte.statements.define :as sut]
   [magritte.statements.format :refer [format-let format-statement]]))

(defn- format-define [expr]
  (sut/format-define expr format-statement))

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

(deftest test-format-define-field
  (is (= "DEFINE FIELD email ON TABLE user"
         (format-define '{:define :field
                          :name   :email
                          :on     [:table :user]})))
  (is (= "DEFINE FIELD email ON TABLE user TYPE string"
         (format-define '{:define :field
                          :name   :email
                          :on     [:table :user]
                          :type   :string})))
  (is (= "DEFINE FIELD email ON TABLE user FLEXIBLE TYPE array<string>"
         (format-define '{:define :field
                          :name   :email
                          :on     [:table :user]
                          :type   [:flexible :array<string>]})))
  (is (= "DEFINE FIELD locked ON TABLE user TYPE option<array<string>> DEFAULT false"
         (format-define '{:define  :field
                          :name    :locked
                          :on      [:table :user]
                          :type    :option<array<string>>
                          :default false})))
  (is (= "DEFINE FIELD updated ON resource VALUE time::now()"
         (format-define '{:define :field
                          :name   :updated
                          :on     :resource
                          :value  (time/now)})))
  (is (= (str "LET $after = make::user();\n"
              "LET $value = (SELECT * FROM user WHERE (id == $after.user));\n"
              "DEFINE FIELD email ON TABLE user TYPE string VALUE string::lowercase($value);")
         (format-let '(let [after (make/user)
                            value {:select [*]
                                   :from   [:user]
                                   :where  (== :id (:user after))}]
                        {:define :field
                         :name   :email
                         :on     [:table :user]
                         :type   :string
                         :value  (string/lowercase value)}))))
  (is (= "DEFINE FIELD email ON TABLE user TYPE string ASSERT string::is::email($value)"
         (format-define '{:define :field
                          :name   :email
                          :on     [:table :user]
                          :type   :string
                          :assert (string/is-email $value)})))
  (is (= "DEFINE FIELD created ON resource VALUE time::now() READONLY"
         (format-define '{:define  :field
                          :name    :created
                          :on      :resource
                          :value   (time/now)
                          :readonly true})))
  (is (= "DEFINE FIELD IF NOT EXISTS email ON TABLE user TYPE string"
         (format-define '{:define [:field :if-not-exists]
                          :name   :email
                          :on     [:table :user]
                          :type   :string})))
  (is (= (str "DEFINE FIELD email ON TABLE user PERMISSIONS "
              "FOR select WHERE ((published = true) OR (user = $auth.id)) "
              "FOR update WHERE (user = $auth.id) "
              "FOR delete WHERE ((user = $auth.id) OR ($auth.role = 'admin'))")
         (format-define '{:define      :field
                          :name        :email
                          :on          [:table :user]
                          :permissions [{:for     :select
                                         :where   (or (= :published true)
                                                      (= :user (:id $auth)))}
                                        {:for     :update
                                         :where   (= :user (:id $auth))}
                                        {:for     :delete
                                         :where   (or (= :user (:id $auth))
                                                      (= (:role $auth) "admin"))}]})))
  (is (= (str "DEFINE FIELD email ON TABLE user PERMISSIONS "
              "FOR select WHERE ((published = true) OR (user = $auth.id)) "
              "FOR update,delete WHERE ((user = $auth.id) OR ($auth.role = 'admin'))")
         (format-define '{:define      :field
                          :name        :email
                          :on          [:table :user]
                          :permissions [{:for     :select
                                         :where   (or (= :published true)
                                                      (= :user (:id $auth)))}
                                        {:for     [:update :delete]
                                         :where   (or (= :user (:id $auth))
                                                      (= (:role $auth) "admin"))}]})))

;
  )

(comment
  (test/run-tests)
  :rcf)
