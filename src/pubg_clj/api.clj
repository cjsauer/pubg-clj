(ns pubg-clj.api
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.core.async
             :as
             async
             :refer
             [<!! >!! chan close! pipeline-async to-chan]]
            [clojure.string :as str]
            [pubg-clj.api.parsers :as p]))

(def ^:dynamic *api-key*)

(defmacro with-api-key
  "Helpful for making a succession of PubG API calls using the same API key.
  Example:
    (with-api-key my-key
      (pubg-fetch ...)
      (pubg-fetch ...))"
  [api-key & body]
  `(binding [*api-key* ~api-key]
     ~@body))

(defn- api-url
  "Constructs a valid PubG API URL given a platform, region, and endpoint. region
  is optional. Query parameters are not included, and should be added by the
  HTTP client mechanism."
  [{:keys [platform region endpoint]}]
  (cond-> "https://api.pubg.com/"
    (or platform region) (concat "shards/")
    platform             (concat platform)
    ;; region               (concat "-" region)
    endpoint             (concat "/" endpoint)
    true str/join))

(defn- parse-response-body
  [resp]
  #_(when-let [remaining (-> resp :headers (get "X-RateLimit-Remaining"))]
    (prn "Remaining " remaining))
  (update resp :body #(json/parse-string % ->kebab-case-keyword)))

(defn pubg-fetch
  "Makes a request to the PubG API using the given api-key, platform,
  region (optional), endpoint and query parameters (qparams). Platform, region
  and endpoint are all used to construct the final URL, but this can be
  overidden using the url key. Returns the response map."
  [{:keys [api-key platform region endpoint qparams url] :as opts}]
  (let [api-key (or api-key *api-key*)
        url (or url (api-url opts))
        resp (http/get url
                       {:accept "application/vnd.api+json"
                        :headers {"Authorization" (str "Bearer " api-key)}
                        :query-params qparams})]
    (parse-response-body resp)))

(defn pubg-fetch-async
  "Asynchronously makes a request to the PubG API using the given api-key, platform,
  region (optional), endpoint and query parameters (qparams). Platform, region
  and endpoint are all used to construct the final URL, but this can be
  overidden with the url key. Passes the response map to succ upon success, or
  exception data to err upon failure."
  [{:keys [api-key platform region endpoint qparams url] :as opts} succ err]
  (let [api-key (or api-key *api-key*)]
    (let [url (or url (api-url opts))]
      (http/get url {:accept "application/vnd.api+json"
                     :headers {"Authorization" (str "Bearer " api-key)}
                     :query-params qparams
                     :async? true}
                (fn [resp]
                  (-> resp parse-response-body succ))
                (comp err ex-data)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Endpoints

(defn- status-endpoint [] "status")

(defn- player-endpoint
  ([] "players")
  ([id] (str "players/" id)))

(defn- match-endpoint [id] (str "matches/" id))
(defn- match-samples-endpoint [] "samples/")

(defn- leaderboard-endpoint
  [game-mode]
  (str "leaderboards/" game-mode))

(defn- seasons-endpoint [] "seasons")
(defn- season-stats-endpoint
  [player-id season-id]
  (str "players/" player-id "/seasons/" season-id))
(defn- season-ranked-stats-endpoint
  [player-id season-id]
  (str "players/" player-id "/seasons/" season-id "/ranked"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Status

(defn status
  "Response map of the API's /status endpoint"
  []
  (pubg-fetch {:endpoint (status-endpoint)}))

(defn online?
  "Returns true if the /status endpoint is returning 200 OK, false otherwise"
  []
  (-> (status) :status (= 200)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Player

(def ^:private player-names-filter-key "filter[playerNames]")
(def ^:private player-ids-filter-key "filter[playerIds]")

(defn- players-fetch
  "Internal implementation of player fetching given a filter query parameter key,
  platform, and player identifier(s) (name or ID). "
  [filter-key platform pident]
  (->> (pubg-fetch {:platform platform
                    :endpoint (player-endpoint)
                    :qparams {filter-key pident}})
       :body
       :data
       (map p/player-parse)))

(defn fetch-player-by-name
  "Retrieves a player by their name on the given platform. Throws if not found."
  [platform player-name]
  (first
   (players-fetch player-names-filter-key platform player-name)))

(defn fetch-player-by-id
  "Retrieves a player by their ID on the given platform. Throws if not found."
  [platform player-id]
  (first
   (players-fetch player-ids-filter-key platform player-id)))

(defn batch-players-by-name
  "Retrieves a batch of players by their names on the given platform. Throws if any
  one of the players is not found. The PubG API limits one batch to 6 player names."
  [platform player-names]
  {:pre [(<= (count player-names) 6)]}
  (players-fetch player-names-filter-key platform (str/join "," player-names)))

(defn batch-players-by-id
  "Retrieves a batch of players by their IDs on the given platform. Throws if any
  one of the players is not found. The PubG API limits one batch to 6 IDs."
  [platform player-ids]
  {:pre [(<= (count player-ids) 6)]}
  (players-fetch player-ids-filter-key platform (str/join "," player-ids)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Match

(defn fetch-match
  "Retrieves a match by its ID on the given platform."
  [platform match-id]
  (->> (pubg-fetch {:platform platform
                    :endpoint (match-endpoint match-id)})
       :body
       p/match-parse))

(defn fetch-match-async
  "Asynchronously retrieves a match by its ID on the given platform, passing the
  result to succ upon success, or passing exception data to err upon failure.
  Exception data will contain ::pubg/match-id for reference."
  [platform match-id succ err]
  (letfn [(wrap-succ [f]
            (fn [resp]
              (-> resp :body p/match-parse f)))
          (wrap-err [f]
            (fn [e]
              (f (merge e {::match-id match-id}))))]
    (pubg-fetch-async {:platform platform
                       :endpoint (match-endpoint match-id)}
                      (wrap-succ succ)
                      (wrap-err err))))

(defn batch-get-matches
  "Fetches the given match IDs for platform in parallel. Optionally accepts an err
  callback that will be passed exception data for every match that fails to
  load. Exception data will contain ::pubg/match-id for reference."
  [platform match-ids & [err]]
  (if (empty? match-ids)
    match-ids
    (let [in> (to-chan match-ids)
          out> (chan (count match-ids))
          core-count (.. Runtime getRuntime availableProcessors)
          af (fn [m-id res>]
               (fetch-match-async platform m-id
                                  (fn [m]
                                    (>!! res> m)
                                    (close! res>))
                                  (fn [e]
                                    (when err (err e)))))]
      (pipeline-async core-count out> af in>)
      (<!! (async/into [] out>)))))

(defn fetch-match-samples
  "Fetches n random matches for the given platform and region in parallel."
  [n platform region]
  (let [match-samples (->> (pubg-fetch {:platform platform
                                        :region region
                                        :endpoint (match-samples-endpoint)})
                           :body :data :relationships :matches :data
                           (map :id))]
    (batch-get-matches platform (take n match-samples))))

(defn fetch-player-matches
  "Given a player, fetch all of their recent matches in parallel. Optionally
  accepts an err callback that will be passed exception data for every match
  that fails to load. Exception data will contain ::pubg/match-id for
  reference."
  [player & [err]]
  (let [{:keys [pubg.player/matches pubg/shard-id]} player
        match-ids                                   (map :pubg.match/id matches)]
    (batch-get-matches shard-id match-ids err)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Telemetry

(defn fetch-match-telemetry
  [match]
  (let [{:keys [pubg.match/telemetry]} match
        url                            (:pubg.match.telemetry/url telemetry)
        events                         (->> (pubg-fetch {:url url})
                                            :body
                                            p/telemetry-events-parse)]
    (assoc events :pubg.match.telemetry/url url)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Leaderboard

(defn fetch-leaderboard
  "Retrieves the leaderboard for the given platform and game mode. Currently
  leaderboards are only available on PC."
  [platform game-mode]
  (->> (pubg-fetch {:platform platform
                    :endpoint (leaderboard-endpoint game-mode)})
       :body
       p/leaderboard-parse))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Seasons and Season Stats

(defn fetch-seasons
  "Fetches a list of available seasons for the given platform."
  [platform]
  (->> (pubg-fetch {:platform platform
                    :endpoint (seasons-endpoint)})
       :body
       :data
       (mapv p/season-parse)))

(defn fetch-current-season
  "Fetches the season that is currently in progress for the given platform."
  [platform]
  (->> (fetch-seasons platform)
       (filter :pubg.season/is-current?)
       first))

(defn fetch-player-season-stats
  "Fetches the season stats for a given player and season. The region is required
  for PS4, Xbox, and for stats of PC players prior to and including
  division.bro.official.2018-09. It is probably best to always include the
  region, as the API will respond with stats for EVERY region in the cases where
  it is depracated."
  [player season-id & [region]]
  (let [{:keys [pubg.player/id pubg/shard-id]} player]
    (->> (pubg-fetch {:platform shard-id
                      :region (or region "")
                      :endpoint (season-stats-endpoint id season-id)})
         :body
         p/player-season-stats-parse)))

(defn fetch-player-season-ranked-stats
  "Fetches the ranked season stats for a given player and season. The
  region is required for PS4, Xbox, and for stats of PC players prior
  to and including division.bro.official.2018-09. It is probably best
  to always include the region, as the API will respond with stats for
  EVERY region in the cases where it is depracated."
  [player season-id & [region]]
  (let [{:keys [pubg.player/id pubg/shard-id]} player]
    (->> (pubg-fetch
          {:platform shard-id,
           :region (or region ""),
           :endpoint (season-ranked-stats-endpoint id season-id)})
         :body
         p/player-season-ranked-stats-parse)))
