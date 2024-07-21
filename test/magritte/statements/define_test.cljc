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
           (format-define '{:define?    :analyzer
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
           (format-define {:define? :database
                           :name    :users}))))
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
           (format-define '{:define? :event
                            :name    :email
                            :on      :user
                            :when    (!= (:email $before) (:email $after))})))))

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
         (format-define '{:define? :field
                          :name    :email
                          :on      [:table :user]
                          :type    :string})))
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
  (is (= (str "DEFINE FIELD email ON TABLE user PERMISSIONS "
              "FOR select WHERE ((published = true) OR (user = $auth.id)) "
              "FOR update,delete WHERE ((user = $auth.id) OR ($auth.role = 'admin'))")
         (format-define '{:define      :field
                          :name        :email
                          :on          [:table :user]
                          :permissions [{:select           (or (= :published true)
                                                               (= :user (:id $auth)))}
                                        {[:update :delete] (or (= :user (:id $auth))
                                                               (= (:role $auth) "admin"))}]})))

  (is (= (str "DEFINE FIELD permissions ON TABLE acl TYPE array "
              "ASSERT ((array::len($value) > 0) AND ($value ALLINSIDE ['create', 'read', 'write', 'delete']))")
         (format-define '{:define    :field
                          :name      :permissions
                          :on        [:table :acl]
                          :type      :array
                          :assert    (and (> (array/len $value) 0)
                                          (allinside $value ["create" "read" "write" "delete"]))})))
  (is (= (str
          "DEFINE FIELD countrycode ON user TYPE string "
          "ASSERT ($value = /[A-Z]{3}/) "
          "VALUE ($value OR $before OR 'GBR')")
         (format-define '{:define   :field
                          :name     :countrycode
                          :on       :user
                          :type     :string
                          :assert   (= $value #"[A-Z]{3}")
                          :value    (or $value $before "GBR")})))
;
  )

(deftest test-format-index
  (testing "unique index"
    (is (= "DEFINE INDEX userEmailIndex ON TABLE user COLUMNS email UNIQUE"
           (format-define '{:define  :index
                            :name    :userEmailIndex
                            :on      [:table :user]
                            :columns [:email]
                            :unique  true}))))
  (testing "non-unique index"
    (is (= "DEFINE INDEX userAgeIndex ON TABLE user COLUMNS age"
           (format-define '{:define  :index
                            :name    :userAgeIndex
                            :on      [:table :user]
                            :columns [:age]}))))
  (testing "composite index"
    (is (= "DEFINE INDEX test ON user FIELDS account, email"
           (format-define '{:define  :index
                            :name    :test
                            :on      :user
                            :fields  [:account :email]}))))
  (testing "full-text search index"
    (is (= "DEFINE INDEX userNameIndex ON TABLE user COLUMNS name SEARCH ANALYZER ascii BM25 HIGHLIGHTS"
           (format-define '{:define           :index
                            :name             :userNameIndex
                            :on               [:table :user]
                            :columns          [:name]
                            :search-analyzer  [:ascii :bm25 :highlights]}))))

  (testing "vector index with 64-bit signed integers"
    (is (= "DEFINE INDEX idx_mtree_embedding ON Document FIELDS items.embedding MTREE DIMENSION 4 TYPE I64"
           (format-define '{:define  :index
                            :name    :idx_mtree_embedding
                            :on      :Document
                            :fields  [(:embedding items)]
                            :mtree   {:dimension 4
                                      :type      :i64}}))))
  (testing "mtree manhattan distance"
    (is (= "DEFINE INDEX idx_mtree_embedding_manhattan ON Document FIELDS items.embedding MTREE DIMENSION 4 DIST MANHATTAN"
           (format-define '{:define  :index
                            :name    :idx_mtree_embedding_manhattan
                            :on      :Document
                            :fields  [(:embedding items)]
                            :mtree   {:dimension 4
                                      :distance  :manhattan}}))))
  (testing "mtree consine distance and capacity"
    (is (= "DEFINE INDEX idx_mtree_embedding_cosine ON Document FIELDS items.embedding MTREE DIMENSION 4 DIST COSINE CAPACITY 100"
           (format-define '{:define  :index
                            :name    :idx_mtree_embedding_cosine
                            :on      :Document
                            :fields  [(:embedding items)]
                            :mtree   {:dimension 4
                                      :distance  :cosine
                                      :capacity  100}}))))

  (testing "HNSW index"
    (is (= "DEFINE INDEX mt_pts ON pts FIELDS point HNSW DIMENSION 4 DIST EUCLIDEAN EFC 150 M 12"
           (format-define '{:define  :index
                            :name    :mt_pts
                            :on      :pts
                            :fields  [:point]
                            :hnsw    {:dimension 4
                                      :distance  :euclidean
                                      :efc       150
                                      :m         12}}))))
  (testing "create index if not exists"
    (is (= "DEFINE INDEX IF NOT EXISTS example ON TABLE user COLUMNS email UNIQUE"
           (format-define '{:define?  :index
                            :name     :example
                            :on       [:table :user]
                            :columns  [:email]
                            :unique   true}))))

;; add more tests
  )

(deftest test-format-define-namespace
  (testing "define namespace"
    (is (= "DEFINE NAMESPACE users"
           (format-define '{:define :namespace
                            :name   :users}))))
  (testing "define namespace if not exists"
    (is (= "DEFINE NAMESPACE IF NOT EXISTS users"
           (format-define '{:define? :namespace
                            :name    :users})))))

(deftest test-format-define-param
  (testing "define param"
    (is (= "DEFINE PARAM $endpointBase VALUE 'https://api.example.com'"
           (format-define '{:define :param
                            :name   :$endpointBase
                            :value  "https://api.example.com"}))))
  (testing "define param if not exists"
    (is (= "DEFINE PARAM IF NOT EXISTS $endpointBase VALUE 'https://api.example.com'"
           (format-define '{:define? :param
                            :name   :$endpointBase
                            :value  "https://api.example.com"})))))

(deftest test-format-define-scope
  (testing "define scope auth"
    (is (= (str "DEFINE SCOPE auth SESSION 24h"
                " SIGNUP (CREATE user SET email = $email, pass = crypto::argon2::generate($pass))"
                " SIGNIN (SELECT * FROM user WHERE ((email = $email) AND crypto::argon2::compare(pass, $pass)))")
           (format-define '{:define   :scope
                            :name     :auth
                            :session  :24h
                            :signup   {:create :user
                                       :set    {:email $email
                                                :pass  (crypto/argon2-generate $pass)}}
                            :signin   {:select [*]
                                       :from   [:user]
                                       :where  (and (= :email $email)
                                                    (crypto/argon2-compare :pass $pass))}})))))

(deftest test-format-define-table
  (testing "define table"
    (is (= "DEFINE TABLE reading"
           (format-define '{:define :table
                            :name   :reading}))))
  (testing "define table with drop"
    (is (= "DEFINE TABLE reading"
           (format-define '{:define :table
                            :name   :reading
                            :drop   true}))))
  (testing "define table with changefeed"
    (is (= "DEFINE TABLE reading CHANGEFEED 3d"
           (format-define '{:define     :table
                            :name       :reading
                            :changefeed :3d}))))
  (testing "define table if not exists"
    (is (= "DEFINE TABLE IF NOT EXISTS reading CHANGEFEED 3d"
           (format-define '{:define?     :table
                            :name        :reading
                            :changefeed  :3d}))))
  (testing "define schemafull table"
    (is (= "DEFINE TABLE user SCHEMAFULL"
           (format-define '{:define     :table
                            :name       :user
                            :schemafull true}))))
  (testing "define schemaless table"
    (is (= "DEFINE TABLE user SCHEMALESS"
           (format-define '{:define     :table
                            :name       :user
                            :schemafull false}))))

  (testing "pre-computed table views"
    (is (= (str "DEFINE TABLE avg_product_review TYPE NORMAL AS "
                "SELECT "
                "count() AS number_of_reviews, "
                "math::mean(<float> rating) AS avg_review, "
                "->product.id AS product_id, "
                "->product.name AS product_name "
                "FROM review "
                "GROUP BY product_id, product_name")
           (format-define '{:define :table
                            :name   :avg_product_review
                            :type   :normal
                            :as     {:select [[(count) :number_of_reviews]
                                              [(math/mean :<float>rating) :avg_review]
                                              [(|-> (:id product)) :product_id]
                                              [(|-> (:name product)) :product_name]]
                                     :from   [:review]
                                     :group  [:product_id :product_name]}}))))
  (testing "defining permissions"
    (is (= (str "DEFINE TABLE post SCHEMALESS "
                "PERMISSIONS "
                "FOR select WHERE ((published = true) OR (user = $auth.id)) "
                "FOR create,update WHERE (user = $auth.id) "
                "FOR delete WHERE ((user = $auth.id) OR ($auth.admin = true))")
           (format-define '{:define      :table
                            :name        :post
                            :schemafull  false
                            :permissions [{:select
                                           (or (= :published true)
                                               (= :user (:id $auth)))}

                                          {[:create :update]
                                           (= :user (:id $auth))}

                                          {:delete
                                           (or (= :user (:id $auth))
                                               (= (:admin $auth) true))}]}))))
  (testing "defining table with relation type, no constraints"
    (is (= "DEFINE TABLE likes TYPE RELATION"
           (format-define '{:define :table
                            :name   :likes
                            :type   :relation}))))
  (testing "defining table with relation type, from to"
    (is (= "DEFINE TABLE likes TYPE RELATION FROM user TO post"
           (format-define '{:define    :table
                            :name      :likes
                            :type     {:relation {:from :user
                                                  :to   :post}}}))))
  (testing "defining table with relation type, in out"
    (is (= (str "DEFINE TABLE assigned_to SCHEMAFULL TYPE RELATION IN tag OUT sticky "
                "PERMISSIONS "
                "FOR create,select,update,delete WHERE ((in.owner = $auth.id) AND (out.author = $auth.id))")
           (format-define '{:define      :table
                            :name        :assigned_to
                            :schemafull   true
                            :type       {:relation   {:in  :tag
                                                      :out :sticky}}
                            :permissions [{[:create :select :update :delete]
                                           (and (= (:owner in) (:id $auth))
                                                (= (:author out) (:id $auth)))}]})))))

(deftest test-format-define-token
  (testing "define token"
    (is (= (str "DEFINE TOKEN token_name "
                "ON DATABASE "
                "TYPE HS512 "
                "VALUE 'sNSYneezcr8kqphfOC6NwwraUHJCVAt0XjsRSNmssBaBRh3WyMa9TRfq8ST7fsU2H2kGiOpU4GbAF1bCiXmM1b3JGgleBzz7rsrz6VvYEM4q3CLkcO8CMBIlhwhzWmy8'")
           (format-define '{:define :token
                            :name   :token_name
                            :on     :database
                            :type   :hs512
                            :value  "sNSYneezcr8kqphfOC6NwwraUHJCVAt0XjsRSNmssBaBRh3WyMa9TRfq8ST7fsU2H2kGiOpU4GbAF1bCiXmM1b3JGgleBzz7rsrz6VvYEM4q3CLkcO8CMBIlhwhzWmy8"}))))
  (testing "if not exists, with scope"
    (is (= "DEFINE TOKEN IF NOT EXISTS example ON SCOPE example TYPE HS512 VALUE 'example'"
           (format-define '{:define?  :token
                            :name     :example
                            :on      {:scope :example}
                            :type     :hs512
                            :value    "example"})))))

(deftest test-format-define-user
  (testing "define user"
    (is (= "DEFINE USER username ON ROOT PASSWORD '123456' ROLES OWNER"
           (format-define '{:define     :user
                            :name       :username
                            :on         :root
                            :password   "123456"
                            :roles      :owner}))))
  ;
  )

;
(comment
  (test/run-tests)
  :rcf)
