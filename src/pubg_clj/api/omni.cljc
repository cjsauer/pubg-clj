(ns pubg-clj.api.omni
  (:require [clojure.spec.alpha :as s])
  (:import [java.time Instant]))

;; TODO: await the arrival of clojure.spec.alpha2 and do away with the :req/:opt keys

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Attribute Definitions
;; - Database schema
;; - Specs
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def pubg-omni
  {
   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; General
   :pubg/game-mode {:db/cardinality :db.cardinality/one
                    :db/valueType :db.type/string
                    :omnigen/spec string?}

   :pubg/shard-id {:db/cardinality :db.cardinality/one
                   :db/valueType :db.type/string
                   :omnigen/spec string?}

   :pubg/title-id {:db/cardinality :db.cardinality/one
                   :db/valueType :db.type/string
                   :omnigen/spec string?}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Player
   :pubg.player/id {:db/cardinality :db.cardinality/one
                    :db/unique :db.unique/identity
                    :db/valueType :db.type/string
                    :omnigen/spec string?}

   :pubg.player/name {:db/cardinality :db.cardinality/one
                      :db/valueType :db.type/string
                      :omnigen/spec string?}

   :pubg.player/patch-version {:db/cardinality :db.cardinality/one
                               :db/valueType :db.type/string
                               :omnigen/spec string?}

   :pubg.player/matches {:db/cardinality :db.cardinality/many
                         :db/valueType :db.type/ref
                         :omnigen/spec (s/coll-of :pubg/match :distinct true)}

   :pubg.player/season-stats {:db/cardinality :db.cardinality/many
                              :db/valueType :db.type/ref
                              :db/isComponent true
                              :omnigen/spec :pubg.season/stats}

   :pubg/player {:omnigen/spec (s/keys :req [:pubg.player/id]
                                       :opt [:pubg.player/name
                                             :pubg/shard-id
                                             :pubg.player/patch-version
                                             :pubg/title-id
                                             :pubg.player/matches])}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Match
   :pubg.match/id {:db/cardinality :db.cardinality/one
                   :db/unique :db.unique/identity
                   :db/valueType :db.type/string
                   :omnigen/spec string?}

   :pubg.match/created-at {:db/cardinality :db.cardinality/one
                           :db/valueType :db.type/instant
                           :omnigen/spec #(instance? Instant %)}

   :pubg.match/duration {:db/cardinality :db.cardinality/one
                         :db/valueType :db.type/long
                         :omnigen/spec int?}

   :pubg.match/map-name {:db/cardinality :db.cardinality/one
                         :db/valueType :db.type/string
                         :omnigen/spec string?}

   :pubg.match/is-custom-match? {:db/cardinality :db.cardinality/one
                                 :db/valueType :db.type/boolean
                                 :omnigen/spec boolean?}

   :pubg.match/season-state {:db/cardinality :db.cardinality/one
                             :db/valueType :db.type/string
                             :omnigen/spec string?}

   :pubg.match/rosters {:db/cardinality :db.cardinality/many
                        :db/valueType :db.type/ref
                        :db/isComponent true
                        :omnigen/spec (s/coll-of :pubg.match/roster :distinct true)}

   :pubg.match/participants {:db/cardinality :db.cardinality/many
                             :db/valueType :db.type/ref
                             :db/isComponent true
                             :omnigen/spec (s/coll-of :pubg.match/participant :distinct true)}

   :pubg/match {:omnigen/spec (s/keys :req [:pubg.match/id]
                                      :opt [:pubg.match/created-at
                                            :pubg.match/duration
                                            :pubg/game-mode
                                            :pubg.match/map-name
                                            :pubg.match/is-custom-match?
                                            :pubg.match/season-state
                                            :pubg/shard-id
                                            :pubg/title-id
                                            :pubg.match/rosters
                                            :pubg.match/participants
                                            :pubg.match/telemetry])}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Roster
   :pubg.match.roster/id {:db/cardinality :db.cardinality/one
                          :db/unique :db.unique/identity
                          :db/valueType :db.type/string
                          :omnigen/spec string?}

   :pubg.match.roster/won? {:db/cardinality :db.cardinality/one
                            :db/valueType :db.type/boolean
                            :omnigen/spec boolean?}

   :pubg.match.roster/rank {:db/cardinality :db.cardinality/one
                            :db/valueType :db.type/long
                            :omnigen/spec int?}

   :pubg.match.roster/team-id {:db/cardinality :db.cardinality/one
                               :db/valueType :db.type/long
                               :omnigen/spec int?}

   :pubg.match.roster/participants {:db/cardinality :db.cardinality/many
                                    :db/valueType :db.type/ref
                                    :omnigen/spec (s/coll-of :pubg.match/participant :distinct true)}

   :pubg.match/roster {:omnigen/spec (s/keys :req [:pubg.match.roster/id]
                                             :opt [:pubg.match.roster/won?
                                                   :pubg.match.roster/rank
                                                   :pubg.match.roster/team-id
                                                   :pubg/shard-id
                                                   :pubg.match.roster/participants])}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Participant
   :pubg.match.participant/id {:db/cardinality :db.cardinality/one
                               :db/unique :db.unique/identity
                               :db/valueType :db.type/string
                               :omnigen/spec string?}

   :pubg.match.participant/name {:db/cardinality :db.cardinality/one
                                 :db/valueType :db.type/string
                                 :omnigen/spec string?}

   :pubg.match.participant/player {:db/cardinality :db.cardinality/one
                                   :db/valueType :db.type/ref
                                   :omnigen/spec :pubg/player}

   :pubg.match.participant.stats/dbnos {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.match.participant.stats/assists {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/long
                                          :omnigen/spec nat-int?}

   :pubg.match.participant.stats/boosts {:db/cardinality :db.cardinality/one
                                         :db/valueType :db.type/long
                                         :omnigen/spec nat-int?}

   :pubg.match.participant.stats/damage-dealt {:db/cardinality :db.cardinality/one
                                               :db/valueType :db.type/float
                                               :omnigen/spec number?}

   :pubg.match.participant.stats/death-type {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/string
                                             :omnigen/spec string?}

   :pubg.match.participant.stats/headshot-kills {:db/cardinality :db.cardinality/one
                                                 :db/valueType :db.type/long
                                                 :omnigen/spec nat-int?}

   :pubg.match.participant.stats/heals {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.match.participant.stats/kills {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.match.participant.stats/kill-place {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/long
                                             :omnigen/spec pos-int?}

   :pubg.match.participant.stats/kill-points {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/long
                                              :omnigen/spec nat-int?}

   :pubg.match.participant.stats/kill-points-delta {:db/cardinality :db.cardinality/one
                                                    :db/valueType :db.type/float
                                                    :omnigen/spec number?}

   :pubg.match.participant.stats/kill-streaks {:db/cardinality :db.cardinality/one
                                               :db/valueType :db.type/long
                                               :omnigen/spec nat-int?}

   :pubg.match.participant.stats/last-kill-points {:db/cardinality :db.cardinality/one
                                                   :db/valueType :db.type/long
                                                   :omnigen/spec nat-int?}

   :pubg.match.participant.stats/last-win-points {:db/cardinality :db.cardinality/one
                                                  :db/valueType :db.type/long
                                                  :omnigen/spec nat-int?}

   :pubg.match.participant.stats/longest-kill {:db/cardinality :db.cardinality/one
                                               :db/valueType :db.type/float
                                               :omnigen/spec number?}

   :pubg.match.participant.stats/most-damage {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/float
                                              :omnigen/spec number?}

   :pubg.match.participant.stats/revives {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/long
                                          :omnigen/spec nat-int?}

   :pubg.match.participant.stats/ride-distance {:db/cardinality :db.cardinality/one
                                                :db/valueType :db.type/float
                                                :omnigen/spec number?}

   :pubg.match.participant.stats/road-kills {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/long
                                             :omnigen/spec nat-int?}

   :pubg.match.participant.stats/swim-distance {:db/cardinality :db.cardinality/one
                                                :db/valueType :db.type/float
                                                :omnigen/spec number?}

   :pubg.match.participant.stats/team-kills {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/long
                                             :omnigen/spec nat-int?}

   :pubg.match.participant.stats/time-survived {:db/cardinality :db.cardinality/one
                                                :db/valueType :db.type/float
                                                :omnigen/spec number?}

   :pubg.match.participant.stats/vehicle-destroys {:db/cardinality :db.cardinality/one
                                                   :db/valueType :db.type/long
                                                   :omnigen/spec nat-int?}

   :pubg.match.participant.stats/walk-distance {:db/cardinality :db.cardinality/one
                                                :db/valueType :db.type/float
                                                :omnigen/spec number?}

   :pubg.match.participant.stats/weapons-acquired {:db/cardinality :db.cardinality/one
                                                   :db/valueType :db.type/long
                                                   :omnigen/spec nat-int?}

   :pubg.match.participant.stats/win-place {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/long
                                            :omnigen/spec pos-int?}

   :pubg.match.participant.stats/win-points {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/long
                                             :omnigen/spec nat-int?}

   :pubg.match.participant.stats/win-points-delta {:db/cardinality :db.cardinality/one
                                                   :db/valueType :db.type/float
                                                   :omnigen/spec number?}

   :pubg.match/participant {:omnigen/spec
                            (s/keys :req [:pubg.match.participant/id]
                                    :opt [:pubg/shard-id
                                          :pubg.match.participant/player
                                          :pubg.match.participant/name
                                          :pubg.match.participant.stats/dbnos
                                          :pubg.match.participant.stats/assists
                                          :pubg.match.participant.stats/boosts
                                          :pubg.match.participant.stats/damage-dealt
                                          :pubg.match.participant.stats/death-type
                                          :pubg.match.participant.stats/headshot-kills
                                          :pubg.match.participant.stats/heals
                                          :pubg.match.participant.stats/kills
                                          :pubg.match.participant.stats/kill-place
                                          :pubg.match.participant.stats/kill-points
                                          :pubg.match.participant.stats/kill-points-delta
                                          :pubg.match.participant.stats/kill-streaks
                                          :pubg.match.participant.stats/last-kill-points
                                          :pubg.match.participant.stats/last-win-points
                                          :pubg.match.participant.stats/longest-kill
                                          :pubg.match.participant.stats/most-damage
                                          :pubg.match.participant.stats/revives
                                          :pubg.match.participant.stats/ride-distance
                                          :pubg.match.participant.stats/road-kills
                                          :pubg.match.participant.stats/swim-distance
                                          :pubg.match.participant.stats/team-kills
                                          :pubg.match.participant.stats/time-survived
                                          :pubg.match.participant.stats/vehicle-destroys
                                          :pubg.match.participant.stats/walk-distance
                                          :pubg.match.participant.stats/weapons-acquired
                                          :pubg.match.participant.stats/win-place
                                          :pubg.match.participant.stats/win-points
                                          :pubg.match.participant.stats/win-points-delta])}


   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Leaderboard

   :pubg.leaderboard/id {:db/cardinality :db.cardinality/one
                         :db/unique :db.unique/identity
                         :db/valueType :db.type/string
                         :omnigen/spec string?}

   :pubg.leaderboard.entry/player {:db/cardinality :db.cardinality/one
                                   :db/valueType :db.type/ref
                                   :omnigen/spec :pubg/player}

   :pubg.leaderboard.entry/rank {:db/cardinality :db.cardinality/one
                                 :db/valueType :db.type/long
                                 :omnigen/spec pos-int?}

   :pubg.leaderboard.entry/name {:db/cardinality :db.cardinality/one
                                 :db/valueType :db.type/string
                                 :omnigen/spec string?}

   :pubg.leaderboard.entry.stats/rank-points {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/float
                                              :omnigen/spec number?}

   :pubg.leaderboard.entry.stats/wins {:db/cardinality :db.cardinality/one
                                       :db/valueType :db.type/long
                                       :omnigen/spec nat-int?}

   :pubg.leaderboard.entry.stats/games {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.leaderboard.entry.stats/win-ratio {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/float
                                            :omnigen/spec number?}

   :pubg.leaderboard.entry.stats/average-damage {:db/cardinality :db.cardinality/one
                                                 :db/valueType :db.type/long
                                                 :omnigen/spec nat-int?}

   :pubg.leaderboard.entry.stats/kills {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.leaderboard.entry.stats/kill-death-ratio {:db/cardinality :db.cardinality/one
                                                   :db/valueType :db.type/float
                                                   :omnigen/spec number?}

   :pubg.leaderboard.entry.stats/average-rank {:db/cardinality :db.cardinality/one
                                               :db/valueType :db.type/float
                                               :omnigen/spec number?}

   :pubg.leaderboard/entry {:omnigen/spec
                            (s/keys :req [:pubg.leaderboard.entry/player
                                          :pubg.leaderboard.entry/name
                                          :pubg.leaderboard.entry/rank
                                          :pubg.leaderboard.entry.stats/rank-points
                                          :pubg.leaderboard.entry.stats/wins
                                          :pubg.leaderboard.entry.stats/games
                                          :pubg.leaderboard.entry.stats/win-ratio
                                          :pubg.leaderboard.entry.stats/average-damage
                                          :pubg.leaderboard.entry.stats/kills
                                          :pubg.leaderboard.entry.stats/kill-death-ratio
                                          :pubg.leaderboard.entry.stats/average-rank])}

   :pubg.leaderboard/entries {:db/cardinality :db.cardinality/many
                              :db/valueType :db.type/ref
                              :db/isComponent true
                              :omnigen/spec (s/coll-of :pubg.leaderboard/entry :distinct true)}

   :pubg/leaderboard {:omnigen/spec
                      (s/keys :req [:pubg.leaderboard/id]
                              :opt [:pubg/shard-id
                                    :pubg/game-mode
                                    :pubg.leaderboard/entries])}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Seasons

   :pubg.season/id {:db/cardinality :db.cardinality/one
                    :db/unique :db.unique/identity
                    :db/valueType :db.type/string
                    :omnigen/spec string?}

   :pubg.season/is-current? {:db/cardinality :db.cardinality/one
                             :db/valueType :db.type/boolean
                             :omnigen/spec boolean?}

   :pubg.season/is-offseason? {:db/cardinality :db.cardinality/one
                               :db/valueType :db.type/boolean
                               :omnigen/spec boolean?}

   :pubg/season {:omnigen/spec (s/keys :req [:pubg.season/id]
                                       :opt [:pubg.season/is-current?
                                             :pubg.season/is-offseason?])}

   ;; Seasons stats

   :pubg.season.stats/assists {:db/cardinality :db.cardinality/one
                               :db/valueType :db.type/long
                               :omnigen/spec nat-int?}

   :pubg.season.stats/best-rank-point {:db/cardinality :db.cardinality/one
                                       :db/valueType :db.type/float
                                       :omnigen/spec number?}

   :pubg.season.stats/boosts {:db/cardinality :db.cardinality/one
                              :db/valueType :db.type/long
                              :omnigen/spec nat-int?}

   :pubg.season.stats/dbnos {:db/cardinality :db.cardinality/one
                             :db/valueType :db.type/long
                             :omnigen/spec nat-int?}

   :pubg.season.stats/daily-kills {:db/cardinality :db.cardinality/one
                                   :db/valueType :db.type/long
                                   :omnigen/spec nat-int?}

   :pubg.season.stats/daily-wins {:db/cardinality :db.cardinality/one
                                  :db/valueType :db.type/long
                                  :omnigen/spec nat-int?}

   :pubg.season.stats/damage-dealt {:db/cardinality :db.cardinality/one
                                    :db/valueType :db.type/float
                                    :omnigen/spec number?}

   :pubg.season.stats/days {:db/cardinality :db.cardinality/one
                            :db/valueType :db.type/long
                            :omnigen/spec nat-int?}

   :pubg.season.stats/headshot-kills {:db/cardinality :db.cardinality/one
                                      :db/valueType :db.type/long
                                      :omnigen/spec nat-int?}

   :pubg.season.stats/heals {:db/cardinality :db.cardinality/one
                             :db/valueType :db.type/long
                             :omnigen/spec nat-int?}

   :pubg.season.stats/kill-points {:db/cardinality :db.cardinality/one
                                   :db/valueType :db.type/float
                                   :omnigen/spec number?}

   :pubg.season.stats/kills {:db/cardinality :db.cardinality/one
                             :db/valueType :db.type/long
                             :omnigen/spec nat-int?}

   :pubg.season.stats/longest-kill {:db/cardinality :db.cardinality/one
                                    :db/valueType :db.type/float
                                    :omnigen/spec number?}

   :pubg.season.stats/longest-time-survived {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/float
                                             :omnigen/spec number?}

   :pubg.season.stats/losses {:db/cardinality :db.cardinality/one
                              :db/valueType :db.type/long
                              :omnigen/spec nat-int?}

   :pubg.season.stats/max-kill-streaks {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.season.stats/most-survival-time {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/float
                                          :omnigen/spec number?}

   :pubg.season.stats/rank-points {:db/cardinality :db.cardinality/one
                                   :db/valueType :db.type/float
                                   :omnigen/spec number?}

   :pubg.season.stats/rank-points-title {:db/cardinality :db.cardinality/one
                                         :db/valueType :db.type/string
                                         :omnigen/spec string?}

   :pubg.season.stats/revives {:db/cardinality :db.cardinality/one
                               :db/valueType :db.type/long
                               :omnigen/spec nat-int?}

   :pubg.season.stats/ride-distance {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/float
                                     :omnigen/spec number?}

   :pubg.season.stats/road-kills {:db/cardinality :db.cardinality/one
                                  :db/valueType :db.type/long
                                  :omnigen/spec nat-int?}

   :pubg.season.stats/round-most-kills {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.season.stats/rounds-played {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/long
                                     :omnigen/spec nat-int?}

   :pubg.season.stats/suicides {:db/cardinality :db.cardinality/one
                                :db/valueType :db.type/long
                                :omnigen/spec nat-int?}

   :pubg.season.stats/swim-distance {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/float
                                     :omnigen/spec number?}

   :pubg.season.stats/team-kills {:db/cardinality :db.cardinality/one
                                  :db/valueType :db.type/long
                                  :omnigen/spec nat-int?}

   :pubg.season.stats/time-survived {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/float
                                     :omnigen/spec number?}

   :pubg.season.stats/top-10s {:db/cardinality :db.cardinality/one
                               :db/valueType :db.type/long
                               :omnigen/spec nat-int?}

   :pubg.season.stats/vehicle-destroys {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.season.stats/walk-distance {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/float
                                     :omnigen/spec number?}

   :pubg.season.stats/weapons-acquired {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec nat-int?}

   :pubg.season.stats/weekly-kills {:db/cardinality :db.cardinality/one
                                    :db/valueType :db.type/long
                                    :omnigen/spec nat-int?}

   :pubg.season.stats/weekly-wins {:db/cardinality :db.cardinality/one
                                   :db/valueType :db.type/long
                                   :omnigen/spec nat-int?}

   :pubg.season.stats/win-points {:db/cardinality :db.cardinality/one
                                  :db/valueType :db.type/float
                                  :omnigen/spec number?}

   :pubg.season.stats/wins {:db/cardinality :db.cardinality/one
                            :db/valueType :db.type/long
                            :omnigen/spec nat-int?}

   :pubg.season/stats {:omnigen/spec
                       (s/coll-of (s/keys :req [:pubg/game-mode
                                                :pubg.season.stats/assists
                                                :pubg.season.stats/best-rank-point
                                                :pubg.season.stats/boosts
                                                :pubg.season.stats/dbnos
                                                :pubg.season.stats/daily-kills
                                                :pubg.season.stats/daily-wins
                                                :pubg.season.stats/damage-dealt
                                                :pubg.season.stats/days
                                                :pubg.season.stats/headshot-kills
                                                :pubg.season.stats/heals
                                                :pubg.season.stats/kill-points
                                                :pubg.season.stats/kills
                                                :pubg.season.stats/longest-kill
                                                :pubg.season.stats/longest-time-survived
                                                :pubg.season.stats/losses
                                                :pubg.season.stats/max-kill-streaks
                                                :pubg.season.stats/most-survival-time
                                                :pubg.season.stats/rank-points
                                                :pubg.season.stats/rank-points-title
                                                :pubg.season.stats/revives
                                                :pubg.season.stats/ride-distance
                                                :pubg.season.stats/road-kills
                                                :pubg.season.stats/round-most-kills
                                                :pubg.season.stats/rounds-played
                                                :pubg.season.stats/suicides
                                                :pubg.season.stats/swim-distance
                                                :pubg.season.stats/team-kills
                                                :pubg.season.stats/time-survived
                                                :pubg.season.stats/top-10s
                                                :pubg.season.stats/vehicle-destroys
                                                :pubg.season.stats/walk-distance
                                                :pubg.season.stats/weapons-acquired
                                                :pubg.season.stats/weekly-kills
                                                :pubg.season.stats/weekly-wins
                                                :pubg.season.stats/win-points
                                                :pubg.season.stats/wins]))}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Telemetry

   :pubg.match.telemetry/url {:db/cardinality :db.cardinality/one
                              :db/unique :db.unique/identity
                              :db/valueType :db.type/string
                              :omnigen/spec (s/nilable string?)}

   :pubg.match.telemetry/events {:db/cardinality :db.cardinality/many
                                 :db/valueType :db.type/ref
                                 :db/isComponent true
                                 :omnigen/spec (s/coll-of :pubg.match.telemetry/event)}

   :pubg.match/telemetry {:db/cardinality :db.cardinality/one
                          :db/valueType :db.type/ref
                          :db/isComponent true
                          :omnigen/spec (s/keys :req [:pubg.match.telemetry/url]
                                                :opt [:pubg.match.telemetry/events])}

   ;; Events

   :pubg.match.telemetry.event/assistant {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/ref
                                          :db/isComponent true
                                          :omnigen/spec :pubg.match.telemetry/character}

   :pubg.match.telemetry.event/attack-id {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/long
                                          :omnigen/spec int?}

   :pubg.match.telemetry.event/attack-type {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/string
                                            :omnigen/spec string?}

   :pubg.match.telemetry.event/attacker  {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/ref
                                          :db/isComponent true
                                          :omnigen/spec :pubg.match.telemetry/character}

   ;; TODO: break this out into its own object type
   :pubg.match.telemetry.event/blue-zone-custom-options {:db/cardinality :db.cardinality/one
                                                         :db/valueType :db.type/string
                                                         :omnigen/spec string?}

   :pubg.match.telemetry.event/camera-view-behaviour {:db/cardinality :db.cardinality/one
                                                      :db/valueType :db.type/string
                                                      :omnigen/spec string?}

   :pubg.match.telemetry.event/character {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/ref
                                          :db/isComponent true
                                          :omnigen/spec :pubg.match.telemetry/character}

   :pubg.match.telemetry.event/characters {:db/cardinality :db.cardinality/many
                                           :db/valueType :db.type/ref
                                           :db/isComponent true
                                           :omnigen/spec (s/coll-of :pubg.match.telemetry/character)}

   :pubg.match.telemetry.event/child-item {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/ref
                                           :db/isComponent true
                                           :omnigen/spec :pubg.match.telemetry/item}

   :pubg.match.telemetry.event/d {:db/cardinality :db.cardinality/one
                                  :db/valueType :db.type/instant
                                  :omnigen/spec #(instance? Instant %)}

   :pubg.match.telemetry.event/dbno-id {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/long
                                        :omnigen/spec int?}

   :pubg.match.telemetry.event/damage {:db/cardinality :db.cardinality/one
                                       :db/valueType :db.type/float
                                       :omnigen/spec number?}

   :pubg.match.telemetry.event/damage-causer-additional-info {:db/cardinality :db.cardinality/many
                                                              :db/valueType :db.type/string
                                                              :omnigen/spec (s/coll-of string?)}

   :pubg.match.telemetry.event/damage-causer-name {:db/cardinality :db.cardinality/one
                                                   :db/valueType :db.type/string
                                                   :omnigen/spec string?}

   :pubg.match.telemetry.event/damage-reason {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/string
                                              :omnigen/spec string?}

   :pubg.match.telemetry.event/damage-type-category {:db/cardinality :db.cardinality/one
                                                     :db/valueType :db.type/string
                                                     :omnigen/spec string?}

   :pubg.match.telemetry.event/distance {:db/cardinality :db.cardinality/one
                                         :db/valueType :db.type/float
                                         :omnigen/spec number?}

   :pubg.match.telemetry.event/drivers {:db/cardinality :db.cardinality/many
                                        :db/valueType :db.type/ref
                                        :db/isComponent true
                                        :omnigen/spec (s/coll-of :pubg.match.telemetry/character)}

   :pubg.match.telemetry.event/elapsed-time {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/float
                                             :omnigen/spec number?}

   :pubg.match.telemetry.event/fire-count {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/long
                                           :omnigen/spec nat-int?}

   :pubg.match.telemetry.event/fire-weapon-stack-count {:db/cardinality :db.cardinality/one
                                                        :db/valueType :db.type/long
                                                        :omnigen/spec int?}

   :pubg.match.telemetry.event/game-state {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/ref
                                           :db/isComponent true
                                           :omnigen/spec :pubg.match.telemetry/game-state}

   :pubg.match.telemetry.event/heal-amount {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/float
                                            :omnigen/spec number?}

   :pubg.match.telemetry.event/is-attacker-in-vehicle? {:db/cardinality :db.cardinality/one
                                                        :db/valueType :db.type/boolean
                                                        :omnigen/spec boolean?}

   :pubg.match.telemetry.event/is-custom-game? {:db/cardinality :db.cardinality/one
                                                :db/valueType :db.type/boolean
                                                :omnigen/spec boolean?}

   :pubg.match.telemetry.event/is-event-mode? {:db/cardinality :db.cardinality/one
                                               :db/valueType :db.type/boolean
                                               :omnigen/spec boolean?}

   :pubg.match.telemetry.event/item {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/ref
                                     :db/isComponent true
                                     :omnigen/spec :pubg.match.telemetry/item}

   :pubg.match.telemetry.event/item-package {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/ref
                                             :db/isComponent true
                                             :omnigen/spec :pubg.match.telemetry/item-package}

   :pubg.match.telemetry.event/killer {:db/cardinality :db.cardinality/one
                                       :db/valueType :db.type/ref
                                       :db/isComponent true
                                       :omnigen/spec :pubg.match.telemetry/character}

   :pubg.match.telemetry.event/map-name {:db/cardinality :db.cardinality/one
                                         :db/valueType :db.type/string
                                         :omnigen/spec string?}

   :pubg.match.telemetry.event/max-speed {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/float
                                          :omnigen/spec number?}

   :pubg.match.telemetry.event/max-swim-depth-of-water {:db/cardinality :db.cardinality/one
                                                        :db/valueType :db.type/float
                                                        :omnigen/spec number?}

   :pubg.match.telemetry.event/num-alive-players {:db/cardinality :db.cardinality/one
                                                  :db/valueType :db.type/long
                                                  :omnigen/spec nat-int?}

   :pubg.match.telemetry.event/object-location {:db/cardinality :db.cardinality/one
                                                :db/valueType :db.type/ref
                                                :db/isComponent true
                                                :omnigen/spec :pubg.match.telemetry/location}

   :pubg.match.telemetry.event/object-type {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/string
                                            :omnigen/spec string?}

   :pubg.match.telemetry.event/owner-team-id {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/long
                                              :omnigen/spec nat-int?}

   :pubg.match.telemetry.event/parent-item {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/ref
                                            :db/isComponent true
                                            :omnigen/spec :pubg.match.telemetry/item}

   :pubg.match.telemetry.event/ping-quality {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/string
                                             :omnigen/spec string?}

   :pubg.match.telemetry.event/reviver {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/ref
                                        :db/isComponent true
                                        :omnigen/spec :pubg.match.telemetry/character}

   :pubg.match.telemetry.event/reward-detail {:db/cardinality :db.cardinality/many
                                              :db/valueType :db.type/ref
                                              :db/isComponent true
                                              :omnigen/spec (s/coll-of :pubg.match.telemetry/reward-detail)}

   :pubg.match.telemetry.event/ride-distance {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/float
                                              :omnigen/spec number?}

   :pubg.match.telemetry.event/season-state {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/string
                                             :omnigen/spec string?}

   :pubg.match.telemetry.event/seat-index {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/long
                                           :omnigen/spec nat-int?}

   :pubg.match.telemetry.event/swim-distance {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/float
                                              :omnigen/spec number?}

   :pubg.match.telemetry.event/t {:db/cardinality :db.cardinality/one
                                  :db/valueType :db.type/string
                                  :omnigen/spec string?}

   :pubg.match.telemetry.event/team-size {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/long
                                          :omnigen/spec nat-int?}

   :pubg.match.telemetry.event/vehicle {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/ref
                                        :db/isComponent true
                                        :omnigen/spec :pubg.match.telemetry/vehicle}

   :pubg.match.telemetry.event/victim {:db/cardinality :db.cardinality/one
                                       :db/valueType :db.type/ref
                                       :db/isComponent true
                                       :omnigen/spec :pubg.match.telemetry/character}

   :pubg.match.telemetry.event/victim-game-result {:db/cardinality :db.cardinality/one
                                                   :db/valueType :db.type/ref
                                                   :db/isComponent true
                                                   :omnigen/spec :pubg.match.telemetry/game-result}

   :pubg.match.telemetry.event/weapon {:db/cardinality :db.cardinality/one
                                       :db/valueType :db.type/ref
                                       :db/isComponent true
                                       :omnigen/spec :pubg.match.telemetry/item}

   :pubg.match.telemetry.event/weapon-id {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/string
                                          :omnigen/spec string?}

   :pubg.match.telemetry.event/weather-id {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/string
                                           :omnigen/spec string?}

   :pubg.match.telemetry/event {:omnigen/spec (s/keys :req [:pubg.match.telemetry.event/d
                                                            :pubg.match.telemetry.event/t])}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; Telemetry Objects

   ;; Common
   :pubg.match.telemetry.common/is-game {:db/cardinality :db.cardinality/one
                                         :db/valueType :db.type/float
                                         :omnigen/spec number?}

   :pubg.match.telemetry/common {:db/cardinality :db.cardinality/one
                                 :db/valueType :db.type/ref
                                 :db/isComponent true
                                 :omnigen/spec (s/keys :req [:pubg.match.telemetry.common/is-game])}

   ;; Character
   :pubg.match.telemetry.character/player {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/ref
                                           :omnigen/spec :pubg/player}

   :pubg.match.telemetry.character/team-id {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/long
                                            :omnigen/spec nat-int?}

   :pubg.match.telemetry.character/health {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/float
                                           :omnigen/spec number?}

   :pubg.match.telemetry.character/location {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/ref
                                             :db/isComponent true
                                             :omnigen/spec :pubg.match.telemetry/location}

   :pubg.match.telemetry.character/ranking {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/long
                                            :omnigen/spec nat-int?}

   :pubg.match.telemetry.character/is-in-blue-zone? {:db/cardinality :db.cardinality/one
                                                     :db/valueType :db.type/boolean
                                                     :omnigen/spec boolean?}

   :pubg.match.telemetry.character/is-in-red-zone? {:db/cardinality :db.cardinality/one
                                                    :db/valueType :db.type/boolean
                                                    :omnigen/spec boolean?}

   :pubg.match.telemetry.character/zone {:db/cardinality :db.cardinality/many
                                         :db/valueType :db.type/string
                                         :omnigen/spec (s/coll-of string?)}

   :pubg.match.telemetry/character {:omnigen/spec (s/keys :req [:pubg.match.telemetry.character/player])}

   ;; GameResult
   :pubg.match.telemetry.game-result/player {:db/cardinality :db.cardinality/one
                                             :db/valueType :db.type/ref
                                             :omnigen/spec :pubg/player}

   :pubg.match.telemetry.game-result/rank {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/long
                                           :omnigen/spec nat-int?}

   :pubg.match.telemetry.game-result/game-result {:db/cardinality :db.cardinality/one
                                                  :db/valueType :db.type/string
                                                  :omnigen/spec string?}

   :pubg.match.telemetry.game-result/team-id {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/long
                                              :omnigen/spec nat-int?}

   :pubg.match.telemetry.game-result/stats {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/ref
                                            :db/isComponent true
                                            :omnigen/spec :pubg.match.telemetry/stats}

   :pubg.match.telemetry/game-result {:omnigen/spec (s/keys :req [:pubg.match.telemetry.game-result/player
                                                                  :pubg.match.telemetry.game-result/rank
                                                                  :pubg.match.telemetry.game-result/game-result
                                                                  :pubg.match.telemetry.game-result/team-id
                                                                  :pubg.match.telemetry.game-result/stats])}

   ;; GameState
   :pubg.match.telemetry.game-state/elapsed-time {:db/cardinality :db.cardinality/one
                                                  :db/valueType :db.type/long
                                                  :omnigen/spec nat-int?}

   :pubg.match.telemetry.game-state/num-alive-teams {:db/cardinality :db.cardinality/one
                                                     :db/valueType :db.type/long
                                                     :omnigen/spec nat-int?}

   :pubg.match.telemetry.game-state/num-join-players {:db/cardinality :db.cardinality/one
                                                      :db/valueType :db.type/long
                                                      :omnigen/spec nat-int?}

   :pubg.match.telemetry.game-state/num-start-players {:db/cardinality :db.cardinality/one
                                                       :db/valueType :db.type/long
                                                       :omnigen/spec nat-int?}

   :pubg.match.telemetry.game-state/num-alive-players {:db/cardinality :db.cardinality/one
                                                       :db/valueType :db.type/long
                                                       :omnigen/spec nat-int?}

   :pubg.match.telemetry.game-state/safety-zone-position {:db/cardinality :db.cardinality/one
                                                          :db/valueType :db.type/ref
                                                          :db/isComponent true
                                                          :omnigen/spec :pubg.match.telemetry/location}

   :pubg.match.telemetry.game-state/safety-zone-radius {:db/cardinality :db.cardinality/one
                                                        :db/valueType :db.type/float
                                                        :omnigen/spec number?}

   :pubg.match.telemetry.game-state/poison-gas-warning-position {:db/cardinality :db.cardinality/one
                                                                 :db/valueType :db.type/ref
                                                                 :db/isComponent true
                                                                 :omnigen/spec :pubg.match.telemetry/location}

   :pubg.match.telemetry.game-state/poison-gas-warning-radius {:db/cardinality :db.cardinality/one
                                                               :db/valueType :db.type/float
                                                               :omnigen/spec number?}

   :pubg.match.telemetry.game-state/red-zone-position {:db/cardinality :db.cardinality/one
                                                       :db/valueType :db.type/ref
                                                       :db/isComponent true
                                                       :omnigen/spec :pubg.match.telemetry/location}

   :pubg.match.telemetry.game-state/red-zone-radius {:db/cardinality :db.cardinality/one
                                                     :db/valueType :db.type/float
                                                     :omnigen/spec number?}

   :pubg.match.telemetry/game-state {:omnigen/spec (s/keys :req [:pubg.match.telemetry.game-state/elapsed-time
                                                                 :pubg.match.telemetry.game-state/num-alive-teams
                                                                 :pubg.match.telemetry.game-state/num-join-players
                                                                 :pubg.match.telemetry.game-state/num-start-players
                                                                 :pubg.match.telemetry.game-state/num-alive-players
                                                                 :pubg.match.telemetry.game-state/safety-zone-position
                                                                 :pubg.match.telemetry.game-state/safety-zone-radius
                                                                 :pubg.match.telemetry.game-state/poison-gas-warning-position
                                                                 :pubg.match.telemetry.game-state/poison-gas-warning-radius
                                                                 :pubg.match.telemetry.game-state/red-zone-position
                                                                 :pubg.match.telemetry.game-state/red-zone-radius])}

   ;; Item
   :pubg.match.telemetry.item/id {:db/cardinality :db.cardinality/one
                                  :db/valueType :db.type/string
                                  :omnigen/spec string?}

   :pubg.match.telemetry.item/stack-count {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/long
                                           :omnigen/spec int?}

   :pubg.match.telemetry.item/category {:db/cardinality :db.cardinality/one
                                        :db/valueType :db.type/string
                                        :omnigen/spec string?}

   :pubg.match.telemetry.item/sub-category {:db/cardinality :db.cardinality/one
                                            :db/valueType :db.type/string
                                            :omnigen/spec string?}

   :pubg.match.telemetry.item/attached-items {:db/cardinality :db.cardinality/many
                                              :db/valueType :db.type/string
                                              :omnigen/spec (s/coll-of :pubg.match.telemetry.item/id)}

   :pubg.match.telemetry/item {:omnigen/spec (s/keys :req [:pubg.match.telemetry.item/id
                                                           :pubg.match.telemetry.item/stack-count
                                                           :pubg.match.telemetry.item/category
                                                           :pubg.match.telemetry.item/sub-category
                                                           :pubg.match.telemetry.item/attached-items])}

   ;; ItemPackage
   :pubg.match.telemetry.item-package/id {:db/cardinality :db.cardinality/one
                                          :db/valueType :db.type/string
                                          :omnigen/spec string?}

   :pubg.match.telemetry.item-package/location {:db/cardinality :db.cardinality/one
                                                :db/valueType :db.type/ref
                                                :db/isComponent true
                                                :omnigen/spec :pubg.match.telemetry/location}

   :pubg.match.telemetry.item-package/items {:db/cardinality :db.cardinality/many
                                             :db/valueType :db.type/ref
                                             :db/isComponent true
                                             :omnigen/spec (s/coll-of :pubg.match.telemetry/item)}

   :pubg.match.telemetry/item-package {:omnigen/spec (s/keys :req [:pubg.match.telemetry.item-package/id
                                                                   :pubg.match.telemetry.item-package/location
                                                                   :pubg.match.telemetry.item-package/items])}

   ;; Location
   :pubg.match.telemetry.location/x {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/float
                                     :omnigen/spec number?}

   :pubg.match.telemetry.location/y {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/float
                                     :omnigen/spec number?}

   :pubg.match.telemetry.location/z {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/float
                                     :omnigen/spec number?}

   :pubg.match.telemetry/location {:omnigen/spec (s/keys :req [:pubg.match.telemetry.location/x
                                                               :pubg.match.telemetry.location/y
                                                               :pubg.match.telemetry.location/z])}

   ;; PlayTimeRecord
   :pubg.match.telemetry.play-time-record/survival-time {:db/cardinality :db.cardinality/one
                                                         :db/valueType :db.type/long
                                                         :omnigen/spec nat-int?}

   :pubg.match.telemetry.play-time-record/team-spectating-time {:db/cardinality :db.cardinality/one
                                                                :db/valueType :db.type/long
                                                                :omnigen/spec nat-int?}

   :pubg.match.telemetry/play-time-record {:omnigen/spec
                                           (s/keys :req [:pubg.match.telemetry.play-time-record/survival-time
                                                         :pubg.match.telemetry.play-time-record/team-spectating-time])}

   ;; RewardDetail
   :pubg.match.telemetry.reward-detail/player {:db/cardinality :db.cardinality/one
                                               :db/valueType :db.type/ref
                                               :omnigen/spec :pubg/player}

   :pubg.match.telemetry.reward-detail/play-time-record {:db/cardinality :db.cardinality/one
                                                         :db/valueType :db.type/ref
                                                         :db/isComponent true
                                                         :omnigen/spec :pubg.match.telemetry/play-time-record}

   :pubg.match.telemetry/reward-detail {:omnigen/spec (s/keys :req [:pubg.match.telemetry.reward-detail/player
                                                                    :pubg.match.telemetry.reward-detail/play-time-record])}

   ;; Stats
   :pubg.match.telemetry.stats/kill-count {:db/cardinality :db.cardinality/one
                                           :db/valueType :db.type/long
                                           :omnigen/spec nat-int?}

   :pubg.match.telemetry.stats/distance-on-foot {:db/cardinality :db.cardinality/one
                                                 :db/valueType :db.type/float
                                                 :omnigen/spec number?}

   :pubg.match.telemetry.stats/distance-on-swim {:db/cardinality :db.cardinality/one
                                                 :db/valueType :db.type/float
                                                 :omnigen/spec number?}

   :pubg.match.telemetry.stats/distance-on-vehicle {:db/cardinality :db.cardinality/one
                                                    :db/valueType :db.type/float
                                                    :omnigen/spec number?}

   :pubg.match.telemetry.stats/distance-on-parachute {:db/cardinality :db.cardinality/one
                                                      :db/valueType :db.type/float
                                                      :omnigen/spec number?}

   :pubg.match.telemetry.stats/distance-on-freefall {:db/cardinality :db.cardinality/one
                                                     :db/valueType :db.type/float
                                                     :omnigen/spec number?}

   :pubg.match.telemetry/stats {:omnigen/spec (s/keys :req [:pubg.match.telemetry.stats/kill-count
                                                            :pubg.match.telemetry.stats/distance-on-foot
                                                            :pubg.match.telemetry.stats/distance-on-swim
                                                            :pubg.match.telemetry.stats/distance-on-vehicle
                                                            :pubg.match.telemetry.stats/distance-on-parachute
                                                            :pubg.match.telemetry.stats/distance-on-freefall])}

   ;; Vehicle
   :pubg.match.telemetry.vehicle/type {:db/cardinality :db.cardinality/one
                                       :db/valueType :db.type/string
                                       :omnigen/spec string?}

   :pubg.match.telemetry.vehicle/id {:db/cardinality :db.cardinality/one
                                     :db/valueType :db.type/string
                                     :omnigen/spec string?}

   :pubg.match.telemetry.vehicle/health-percent {:db/cardinality :db.cardinality/one
                                                 :db/valueType :db.type/float
                                                 :omnigen/spec number?}

   :pubg.match.telemetry.vehicle/fuel-percent {:db/cardinality :db.cardinality/one
                                               :db/valueType :db.type/float
                                               :omnigen/spec number?}

   :pubg.match.telemetry/vehicle {:omnigen/spec (s/keys :req [:pubg.match.telemetry.vehicle/type
                                                              :pubg.match.telemetry.vehicle/id
                                                              :pubg.match.telemetry.vehicle/health-percent
                                                              :pubg.match.telemetry.vehicle/fuel-percent])}
   })

