(ns pubg-clj.api.parsers
  (:import [java.time Instant])
  (:require [clojure.walk :as walk]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; API Response Parsing
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro defparser
  [sym parse-map]
  `(defn ~sym [~'data]
     (let [exec# (fn [{:keys [~'from ~'using] :as instr#}]
                   (let [res# (get-in ~'data ~'from)]
                     (when-not (nil? res#)
                       (if ~'using (~'using res#) res#))))]
       (->> (map #(vector (first %) (exec# (second %)))
                 ~parse-map)
            (into {})))))

(defn- is-roster?
  [e]
  (= "roster" (:type e)))

(defn- is-participant?
  [e]
  (= "participant" (:type e)))

(defparser roster-parse
  {:pubg.match.roster/id           {:from [:id]}
   :pubg/shard-id                  {:from [:attributes :shard-id]}
   :pubg.match.roster/won?         {:from [:attributes :won]
                                    :using #(Boolean/parseBoolean %)}
   :pubg.match.roster/rank         {:from [:attributes :stats :rank]}
   :pubg.match.roster/team-id      {:from [:attributes :stats :team-id]}
   :pubg.match.roster/participants {:from [:relationships :participants :data]
                                    :using (partial mapv #(hash-map :pubg.match.participant/id (:id %)))}})

(defparser participant-parse
  {:pubg.match.participant/id                         {:from [:id]}
   :pubg/shard-id                                     {:from [:attributes :shard-id]}
   :pubg.match.participant/player                     {:from [:attributes :stats :player-id]
                                                       :using #(hash-map :pubg.player/id %)}
   :pubg.match.participant/name                       {:from [:attributes :stats :name]}
   :pubg.match.participant.stats/dbnos                {:from [:attributes :stats :dbn-os]}
   :pubg.match.participant.stats/assists              {:from [:attributes :stats :assists]}
   :pubg.match.participant.stats/boosts               {:from [:attributes :stats :boosts]}
   :pubg.match.participant.stats/damage-dealt         {:from [:attributes :stats :damage-dealt]}
   :pubg.match.participant.stats/death-type           {:from [:attributes :stats :death-type]}
   :pubg.match.participant.stats/headshot-kills       {:from [:attributes :stats :headshot-kills]}
   :pubg.match.participant.stats/heals                {:from [:attributes :stats :heals]}
   :pubg.match.participant.stats/kills                {:from [:attributes :stats :kills]}
   :pubg.match.participant.stats/kill-place           {:from [:attributes :stats :kill-place]}
   :pubg.match.participant.stats/kill-points          {:from [:attributes :stats :kill-points]}
   :pubg.match.participant.stats/kill-points-delta    {:from [:attributes :stats :kill-points-delta]}
   :pubg.match.participant.stats/kill-streaks         {:from [:attributes :stats :kill-streaks]}
   :pubg.match.participant.stats/last-kill-points     {:from [:attributes :stats :last-kill-points]}
   :pubg.match.participant.stats/last-win-points      {:from [:attributes :stats :last-win-points]}
   :pubg.match.participant.stats/longest-kill         {:from [:attributes :stats :longest-kill]}
   :pubg.match.participant.stats/most-damage          {:from [:attributes :stats :most-damage]}
   :pubg.match.participant.stats/revives              {:from [:attributes :stats :revives]}
   :pubg.match.participant.stats/ride-distance        {:from [:attributes :stats :ride-distance]}
   :pubg.match.participant.stats/road-kills           {:from [:attributes :stats :road-kills]}
   :pubg.match.participant.stats/swim-distance        {:from [:attributes :stats :swim-distance]}
   :pubg.match.participant.stats/team-kills           {:from [:attributes :stats :team-kills]}
   :pubg.match.participant.stats/time-survived        {:from [:attributes :stats :time-survived]}
   :pubg.match.participant.stats/vehicle-destroys     {:from [:attributes :stats :vehicle-destroys]}
   :pubg.match.participant.stats/walk-distance        {:from [:attributes :stats :walk-distance]}
   :pubg.match.participant.stats/weapons-acquired     {:from [:attributes :stats :weapons-acquired]}
   :pubg.match.participant.stats/win-place            {:from [:attributes :stats :win-place]}
   :pubg.match.participant.stats/win-points           {:from [:attributes :stats :win-points]}
   :pubg.match.participant.stats/win-points-delta     {:from [:attributes :stats :win-points-delta]}
   })

(defn- find-telemetry-url
  [mdata]
  (let [tel-id (-> mdata :data :relationships :assets :data first :id)
        tel-obj (first (filter #(= tel-id (:id %)) (:included mdata)))]
    (get-in tel-obj [:attributes :url])))

(defparser match-parse
  {:pubg.match/id               {:from [:data :id]}
   :pubg.match/created-at       {:from [:data :attributes :created-at]
                                 :using #(Instant/parse %)}
   :pubg.match/duration         {:from [:data :attributes :duration]}
   :pubg/game-mode              {:from [:data :attributes :game-mode]}
   :pubg.match/map-name         {:from [:data :attributes :map-name]}
   :pubg.match/is-custom-match? {:from [:data :attributes :is-custom-match]}
   :pubg.match/season-state     {:from [:data :attributes :season-state]}
   :pubg/shard-id               {:from [:data :attributes :shard-id]}
   :pubg/title-id               {:from [:data :attributes :title-id]}
   :pubg.match/rosters          {:from [:included]
                                 :using #(mapv roster-parse (filter is-roster? %))}
   :pubg.match/participants     {:from [:included]
                                 :using #(mapv participant-parse (filter is-participant? %))}
   :pubg.match/telemetry        {:from []
                                 :using (comp #(hash-map :pubg.match.telemetry/url %)
                                              find-telemetry-url)}})

(defparser player-parse
  {:pubg.player/id            {:from [:id]}
   :pubg.player/name          {:from [:attributes :name]}
   :pubg/shard-id             {:from [:attributes :shard-id]}
   :pubg.player/patch-version {:from [:attributes :patch-version]}
   :pubg/title-id             {:from [:attributes :title-id]}
   :pubg.player/matches       {:from [:relationships :matches :data]
                               :using (partial mapv #(hash-map :pubg.match/id (:id %)))}})

(defparser leaderboard-entry-parse
  {:pubg.leaderboard.entry/player                 {:from [:id]
                                                   :using #(hash-map :pubg.player/id %)}
   :pubg.leaderboard.entry/name                   {:from [:attributes :name]}
   :pubg.leaderboard.entry/rank                   {:from [:attributes :rank]}
   :pubg.leaderboard.entry.stats/rank-points      {:from [:attributes :stats :rank-points]}
   :pubg.leaderboard.entry.stats/wins             {:from [:attributes :stats :wins]}
   :pubg.leaderboard.entry.stats/games            {:from [:attributes :stats :games]}
   :pubg.leaderboard.entry.stats/win-ratio        {:from [:attributes :stats :win-ratio]}
   :pubg.leaderboard.entry.stats/average-damage   {:from [:attributes :stats :average-damage]}
   :pubg.leaderboard.entry.stats/kills            {:from [:attributes :stats :kills]}
   :pubg.leaderboard.entry.stats/kill-death-ratio {:from [:attributes :stats :kill-death-ratio]}
   :pubg.leaderboard.entry.stats/average-rank     {:from [:attributes :stats :average-rank]}})

(defparser leaderboard-parse
  {:pubg.leaderboard/id       {:from [:data :id]}
   :pubg/shard-id             {:from [:data :attributes :shard-id]}
   :pubg/game-mode            {:from [:data :attributes :game-mode]}
   :pubg.leaderboard/entries  {:from [:included]
                               :using #(sort-by :pubg.leaderboard.entry/rank
                                                (map leaderboard-entry-parse %))}})

(defparser season-parse
  {:pubg.season/id            {:from [:id]}
   :pubg.season/is-current?   {:from [:attributes :is-current-season]}
   :pubg.season/is-offseason? {:from [:attributes :is-current-season]}})

(defparser season-stats-parse
  {:pubg/game-mode                           {:from [:game-mode]}
   :pubg.season.stats/assists                {:from [:assists]}
   :pubg.season.stats/best-rank-point        {:from [:best-rank-point]}
   :pubg.season.stats/boosts                 {:from [:boosts]}
   :pubg.season.stats/dbnos                  {:from [:d-bn-os]}
   :pubg.season.stats/daily-kills            {:from [:daily-kills]}
   :pubg.season.stats/daily-wins             {:from [:daily-wins]}
   :pubg.season.stats/damage-dealt           {:from [:damage-dealt]}
   :pubg.season.stats/days                   {:from [:days]}
   :pubg.season.stats/headshot-kills         {:from [:headshot-kills]}
   :pubg.season.stats/heals                  {:from [:heals]}
   :pubg.season.stats/kill-points            {:from [:kill-points]}
   :pubg.season.stats/kills                  {:from [:kills]}
   :pubg.season.stats/longest-kill           {:from [:longest-kill]}
   :pubg.season.stats/longest-time-survived  {:from [:longest-time-survived]}
   :pubg.season.stats/losses                 {:from [:losses]}
   :pubg.season.stats/max-kill-streaks       {:from [:max-kill-streaks]}
   :pubg.season.stats/most-survival-time     {:from [:most-survival-time]}
   :pubg.season.stats/rank-points            {:from [:rank-points]}
   :pubg.season.stats/rank-points-title      {:from [:rank-points-title]}
   :pubg.season.stats/revives                {:from [:revives]}
   :pubg.season.stats/ride-distance          {:from [:ride-distance]}
   :pubg.season.stats/road-kills             {:from [:road-kills]}
   :pubg.season.stats/round-most-kills       {:from [:round-most-kills]}
   :pubg.season.stats/rounds-played          {:from [:rounds-played]}
   :pubg.season.stats/suicides               {:from [:suicides]}
   :pubg.season.stats/swim-distance          {:from [:swim-distance]}
   :pubg.season.stats/team-kills             {:from [:team-kills]}
   :pubg.season.stats/time-survived          {:from [:time-survived]}
   :pubg.season.stats/top-10s                {:from [:top-10s]}
   :pubg.season.stats/vehicle-destroys       {:from [:vehicle-destroys]}
   :pubg.season.stats/walk-distance          {:from [:walk-distance]}
   :pubg.season.stats/weapons-acquired       {:from [:weapons-acquired]}
   :pubg.season.stats/weekly-kills           {:from [:weekly-kills]}
   :pubg.season.stats/weekly-wins            {:from [:weekly-wins]}
   :pubg.season.stats/win-points             {:from [:win-points]}
   :pubg.season.stats/wins                   {:from [:wins]}
   })

(defparser season-ranked-stats-parse
 {:pubg/game-mode                         {:from [:game-mode]},
  :pubg.season.stats/kills                {:from [:kills]},
  :pubg.season.stats/kill-streak          {:from [:kill-streak]},
  :pubg.season.stats/boosts               {:from [:boosts]},
  :pubg.season.stats/team-kills           {:from [:team-kills]},
  :pubg.season.stats/revives              {:from [:revives]},
  :pubg.season.stats/assists              {:from [:assists]},
  :pubg.season.stats/kdr                  {:from [:kdr]},
  :pubg.season.stats/revive-ratio         {:from [:revive-ratio]},
  :pubg.season.stats/avg-rank             {:from [:avg-rank]},
  :pubg.season.stats/deaths               {:from [:deaths]},
  :pubg.season.stats/damage-dealt         {:from [:damage-dealt]},
  :pubg.season.stats/weapons-acquired     {:from [:weapons-acquired]},
  :pubg.season.stats/heals                {:from [:heals]},
  :pubg.season.stats/play-time            {:from [:play-time]},
  :pubg.season.stats/top-10-ratio         {:from [:top-10-ratio]},
  :pubg.season.stats/kda                  {:from [:kda]},
  :pubg.season.stats/headshot-kill-ratio  {:from [:headshot-kill-ratio]},
  :pubg.season.stats/current-rank-point   {:from [:current-rank-point]},
  :pubg.season.stats/dbnos                {:from [:d-bn-os]},
  :pubg.season.stats/best-tier            {:from [:best-tier :tier]},
  :pubg.season.stats/avg-survival-time    {:from [:avg-survival-time]},
  :pubg.season.stats/round-most-kills     {:from [:round-most-kills]},
  :pubg.season.stats/headshot-kills       {:from [:headshot-kills]},
  :pubg.season.stats/current-tier         {:from [:current-tier :tier]},
  :pubg.season.stats/current-sub-tier     {:from [:current-tier :sub-tier]},
  :pubg.season.stats/best-rank-point      {:from [:best-rank-point]},
  :pubg.season.stats/longest-kill         {:from [:longest-kill]},
  :pubg.season.stats/wins                 {:from [:wins]},
  :pubg.season.stats/win-ratio            {:from [:win-ratio]},
  :pubg.season.stats/rounds-played        {:from [:rounds-played]}})

(defn- pack-game-mode-stats
  [[game-mode-key stats-map]]
  (assoc stats-map :game-mode (name game-mode-key)))

(defparser player-season-stats-parse
  {:pubg.player/id    {:from [:data :relationships :player :data :id]}
   :pubg.player/season-stats {:from [:data :attributes :game-mode-stats]
                              :using #(let [stats (map pack-game-mode-stats %)]
                                        (mapv season-stats-parse stats))}})

(defparser
 player-season-ranked-stats-parse
 {:pubg.player/id                  {:from [:data :relationships :player :data :id]},
  :pubg.player/season-ranked-stats {:from [:data :attributes :ranked-game-mode-stats]
                             :using #(let [stats (map pack-game-mode-stats %)]
                                       (mapv season-ranked-stats-parse stats))}})

(defparser telemetry-common-parse
  {:pubg.match.telemetry.common/is-game {:from [:is-game]}})

(defparser telemetry-location-parse
  {:pubg.match.telemetry.location/x {:from [:x]}
   :pubg.match.telemetry.location/y {:from [:y]}
   :pubg.match.telemetry.location/z {:from [:z]}})

(defparser telemetry-character-parse
  {:pubg.match.telemetry.character/player           {:from  []
                                                     :using #(hash-map :pubg.player/id (:account-id %)
                                                                       :pubg.player/name (:name %))}
   :pubg.match.telemetry.character/team-id          {:from [:team-id]}
   :pubg.match.telemetry.character/health           {:from [:health]}
   :pubg.match.telemetry.character/location         {:from  [:location]
                                                     :using telemetry-location-parse}
   :pubg.match.telemetry.character/ranking          {:from [:ranking]}
   :pubg.match.telemetry.character/is-in-blue-zone? {:from [:is-in-blue-zone]}
   :pubg.match.telemetry.character/is-in-red-zone?  {:from [:is-in-red-zone]}
   :pubg.match.telemetry.character/zone             {:from [:zone]}})

(defparser telemetry-item-parse
  {:pubg.match.telemetry.item/id             {:from [:item-id]}
   :pubg.match.telemetry.item/stack-count    {:from [:stack-count]}
   :pubg.match.telemetry.item/category       {:from [:category]}
   :pubg.match.telemetry.item/sub-category   {:from [:sub-category]}
   :pubg.match.telemetry.item/attached-items {:from [:attached-items]}})

(defparser telemetry-item-package-parse
  {:pubg.match.telemetry.item-package/id       {:from [:item-package-id]}
   :pubg.match.telemetry.item-package/location {:from  [:location]
                                                :using telemetry-location-parse}
   :pubg.match.telemetry.item-package/items    {:from  [:items]
                                                :using #(mapv telemetry-item-parse %)}})

(defparser telemetry-stats-parse
  {:pubg.match.telemetry.stats/kill-count            {:from [:kill-count]}
   :pubg.match.telemetry.stats/distance-on-foot      {:from [:distance-on-foot]}
   :pubg.match.telemetry.stats/distance-on-swim      {:from [:distance-on-swim]}
   :pubg.match.telemetry.stats/distance-on-vehicle   {:from [:distance-on-vehicle]}
   :pubg.match.telemetry.stats/distance-on-parachute {:from [:distance-on-parachute]}
   :pubg.match.telemetry.stats/distance-on-freefall  {:from [:distance-on-freefall]}})

(defparser telemetry-game-result-parse
  {:pubg.match.telemetry.game-result/player      {:from  []
                                                  :using #(hash-map :pubg.player/id (:account-id %))}
   :pubg.match.telemetry.game-result/rank        {:from [:rank]}
   :pubg.match.telemetry.game-result/game-result {:from [:game-result]}
   :pubg.match.telemetry.game-result/team-id     {:from [:team-id]}
   :pubg.match.telemetry.game-result/stats       {:from  [:stats]
                                                  :using telemetry-stats-parse}})

(defparser telemetry-game-state-parse
  {:pubg.match.telemetry.game-state/elapsed-time                {:from [:elapsed-time]}
   :pubg.match.telemetry.game-state/num-alive-teams             {:from [:num-alive-teams]}
   :pubg.match.telemetry.game-state/num-join-players            {:from [:num-join-players]}
   :pubg.match.telemetry.game-state/num-start-players           {:from [:num-start-players]}
   :pubg.match.telemetry.game-state/num-alive-players           {:from [:num-alive-players]}
   :pubg.match.telemetry.game-state/safety-zone-position        {:from  [:safety-zone-position]
                                                                 :using telemetry-location-parse}
   :pubg.match.telemetry.game-state/safety-zone-radius          {:from [:safety-zone-radius]}
   :pubg.match.telemetry.game-state/poison-gas-warning-position {:from  [:poison-gas-warning-position]
                                                                 :using telemetry-location-parse}
   :pubg.match.telemetry.game-state/poison-gas-warning-radius   {:from [:poison-gas-warning-radius]}
   :pubg.match.telemetry.game-state/red-zone-position           {:from  [:red-zone-position]
                                                                 :using telemetry-location-parse}
   :pubg.match.telemetry.game-state/red-zone-radius             {:from [:red-zone-radius]}})

(defparser telemetry-play-time-record-parse
  {:pubg.match.telemetry.play-time-record/survival-time        {:from [:survival-time]}
   :pubg.match.telemetry.play-time-record/team-spectating-time {:from [:team-spectating-time]}})

(defparser telemetry-reward-detail-parse
  {:pubg.match.telemetry.reward-detail/player           {:from  []
                                                         :using #(hash-map :pubg.player/id (:account-id %))}
   :pubg.match.telemetry.reward-detail/play-time-record {:from  [:play-time-record]
                                                         :using telemetry-play-time-record-parse}})

(defparser telemetry-vehicle-parse
  {:pubg.match.telemetry.vehicle/type           {:from [:vehicle-type]}
   :pubg.match.telemetry.vehicle/id             {:from [:vehicle-id]}
   :pubg.match.telemetry.vehicle/health-percent {:from [:health-percent]}
   :pubg.match.telemetry.vehicle/fuel-percent   {:from [:feul-percent]} ;; mispelling intentional
   })

(defparser telemetry-event-parse
  {:pubg.match.telemetry.event/player                   {:from  []
                                                         :using #(when-let [id (:account-id %)]
                                                                   (hash-map :pubg.player/id id))}
   :pubg.match.telemetry.event/match                    {:from  []
                                                         :using #(when-let [id (:match-id %)]
                                                                   (hash-map :pubg.match/id id))}
   :pubg.match.telemetry.event/assistant                {:from  [:assistant]
                                                         :using telemetry-character-parse}
   :pubg.match.telemetry.event/attack-id                {:from [:attack-id]}
   :pubg.match.telemetry.event/attack-type              {:from [:attack-type]}
   :pubg.match.telemetry.event/attacker                 {:from  [:attacker]
                                                         :using telemetry-character-parse}
   ;; TODO: break this out into its own object type
   :pubg.match.telemetry.event/blue-zone-custom-options {:from [:blue-zone-custom-options]}
   :pubg.match.telemetry.event/camera-view-behaviour    {:from [:camera-view-behaviour]}
   :pubg.match.telemetry/common                         {:from  [:common]
                                                         :using telemetry-common-parse}

   :pubg.match.telemetry.event/character                     {:from  [:character]
                                                              :using telemetry-character-parse}
   :pubg.match.telemetry.event/characters                    {:from  [:characters]
                                                              :using #(mapv telemetry-character-parse %)}
   :pubg.match.telemetry.event/child-item                    {:from  [:child-item]
                                                              :using telemetry-item-parse}
   :pubg.match.telemetry.event/d                             {:from  [:d]
                                                              :using #(Instant/parse %)}
   :pubg.match.telemetry.event/dbno-id                       {:from [:d-bno-id]}
   :pubg.match.telemetry.event/damage                        {:from [:damage]}
   :pubg.match.telemetry.event/damage-causer-additional-info {:from [:damage-causer-additional-info]}
   :pubg.match.telemetry.event/damage-causer-name            {:from [:damage-causer-name]}
   :pubg.match.telemetry.event/damage-reason                 {:from [:damage-reason]}
   :pubg.match.telemetry.event/damage-type-category          {:from [:damage-type-category]}
   :pubg.match.telemetry.event/distance                      {:from [:distance]}
   :pubg.match.telemetry.event/drivers                       {:from  [:drivers]
                                                              :using #(mapv telemetry-character-parse %)}
   :pubg.match.telemetry.event/elapsed-time                  {:from [:elapsed-time]}
   :pubg.match.telemetry.event/fire-count                    {:from [:fire-count]}
   :pubg.match.telemetry.event/fire-weapon-stack-count       {:from [:fire-weapon-stack-count]}
   :pubg.match.telemetry.event/game-state                    {:from  [:game-state]
                                                              :using telemetry-game-state-parse}
   :pubg.match.telemetry.event/heal-amount                   {:from [:heal-amount]}
   :pubg.match.telemetry.event/is-attacker-in-vehicle?       {:from [:is-attacker-in-vehicle]}
   :pubg.match.telemetry.event/is-custom-game?               {:from [:is-custom-game]}
   :pubg.match.telemetry.event/is-event-mode?                {:from [:is-event-mode]}
   :pubg.match.telemetry.event/item                          {:from  [:item]
                                                              :using telemetry-item-parse}
   :pubg.match.telemetry.event/item-package                  {:from  [:item-package]
                                                              :using telemetry-item-package-parse}
   :pubg.match.telemetry.event/killer                        {:from  [:killer]
                                                              :using telemetry-character-parse}
   :pubg.match.telemetry.event/map-name                      {:from [:map-name]}
   :pubg.match.telemetry.event/max-speed                     {:from [:max-speed]}
   :pubg.match.telemetry.event/max-swim-depth-of-water       {:from [:max-swim-depth-of-water]}
   :pubg.match.telemetry.event/num-alive-players             {:from [:num-alive-players]}
   :pubg.match.telemetry.event/object-location               {:from  [:object-location]
                                                              :using telemetry-location-parse}
   :pubg.match.telemetry.event/object-type                   {:from [:object-type]}
   :pubg.match.telemetry.event/owner-team-id                 {:from [:owner-team-id]}
   :pubg.match.telemetry.event/parent-item                   {:from  [:parent-item]
                                                              :using telemetry-item-parse}
   :pubg.match.telemetry.event/ping-quality                  {:from [:ping-quality]}
   :pubg.match.telemetry.event/reviver                       {:from  [:reviver]
                                                              :using telemetry-character-parse}
   :pubg.match.telemetry.event/reward-detail                 {:from  [:reward-detail]
                                                              :using #(mapv telemetry-reward-detail-parse %)}
   :pubg.match.telemetry.event/ride-distance                 {:from [:ride-distance]}
   :pubg.match.telemetry.event/season-state                  {:from [:season-state]}
   :pubg.match.telemetry.event/seat-index                    {:from [:seat-index]}
   :pubg.match.telemetry.event/swim-distance                 {:from [:swim-distance]}
   :pubg.match.telemetry.event/t                             {:from [:t]}
   :pubg.match.telemetry.event/team-size                     {:from [:team-size]}
   :pubg.match.telemetry.event/vehicle                       {:from  [:vehicle]
                                                              :using telemetry-vehicle-parse}
   :pubg.match.telemetry.event/victim                        {:from  [:victim]
                                                              :using telemetry-character-parse}
   :pubg.match.telemetry.event/victim-game-result            {:from  [:victim-game-result]
                                                              :using telemetry-game-result-parse}
   :pubg.match.telemetry.event/weapon                        {:from  [:weapon]
                                                              :using telemetry-item-parse}
   :pubg.match.telemetry.event/weapon-id                     {:from [:weapon-id]}
   :pubg.match.telemetry.event/weather-id                    {:from [:weather-id]}
   })

(defn- discard-nil-vals
  [m]
  (into {} (map (fn [[k v]]
                  (when-not (nil? v)
                    [k v]))
                m)))

(defparser telemetry-events-parse
  {:pubg.match.telemetry/events {:from  []
                                 :using #(let [evts          (mapv telemetry-event-parse %)
                                               maybe-discard (fn [x]
                                                               (if (map? x) (discard-nil-vals x) x))]
                                           (walk/postwalk maybe-discard evts))}})
