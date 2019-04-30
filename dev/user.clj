(ns user
  (:require [pubg-clj.api :as pubg]
            [pubg-clj.omnigen :as o]
            [pubg-clj.api.omni :refer [pubg-omni]]
            [datascript.core :as d]))

;; You can add your API key to this file as a convenience measure.
;; It is ignored by git.
(def my-api-key (slurp "api_key.txt"))

(def conn
  (let [schema (o/datascript-schema pubg-omni)]
    (d/create-conn schema)))

(comment

  ;; Fetch a player
  (def player
    (pubg/with-api-key my-api-key
      (pubg/fetch-player-by-name "pc" "shroud")))

  ;; Transact it against our Datascript connection
  (d/transact! conn [player])

  ;; Test out queries
  (d/q '[:find [?name ...]
         :where
         [?e :pubg.player/name ?name]]
       @conn)

  ;; Fetch all of Shroud's latest matches
  (def matches
    (pubg/with-api-key my-api-key
      (pubg/fetch-player-matches player)))

  ;; Slowwww...
  (d/transact! conn matches)

  (count matches)

  ;; Total up all of Shroud's latest kills
  (d/q '[:find (sum ?kills) .
         :where
         [?p :pubg.match.participant/name "shroud"]
         [?p :pubg.match.participant.stats/kills ?kills]]
       @conn)

  )
