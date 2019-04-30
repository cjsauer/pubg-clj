(ns pubg-clj.api-test
  (:require [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [pubg-clj.api :as pubg]
            [pubg-clj.api.parsers :as p]
            [pubg-clj.api.spec]))

(def fixture-path "fixtures.edn")

(def ^:dynamic *fixtures*)

(defn- load-fixture
  []
  (-> fixture-path slurp read-string))

(defn unparsed-fixtures
  [f]
  (binding [*fixtures* (load-fixture)]
    (f)))

(use-fixtures :once unparsed-fixtures)

(deftest test-parsers
  (testing "Top-level parsers"
    (are [spec result] (every? true? (map (partial s/valid? spec) result))
      :pubg/player      [(p/player-parse (::player *fixtures*))]
      :pubg/match       (map p/match-parse  (::matches *fixtures*))
      :pubg/leaderboard [(p/leaderboard-parse (::leaderboard *fixtures*))])))

(defn- generate-fixtures
  "Fetches sample responses from the PubG API and spits them to disk. "
  []
  (with-redefs [;; Fetch, but with parsing disabled
                p/player-parse      identity
                p/match-parse       identity
                p/leaderboard-parse identity]
    (let [p (pubg/fetch-player-by-name "pc" "shroud")
          ms (pubg/fetch-match-samples 5 "xbox" "na")
          lbrd (pubg/fetch-leaderboard "steam" "squad")]
      (spit fixture-path
            (with-out-str
              (pp/pprint {::player p
                          ::matches ms
                          ::leaderboard lbrd}))))))

(comment
  (def my-api-key (slurp "api_key.txt"))

  ;; Plop some unparsed API responses to disk
  (pubg/with-api-key my-api-key (generate-fixtures))

  )
