(ns pubg-clj.omnigen
  (:require [clojure.spec.alpha :as s]))

(defn datascript-schema
  "Return a valid datascript schema given an omni map."
  [omni]
  (when-let [omni-kvs (seq omni)]
    (letfn [(select-compat [[k {:db/keys [valueType unique cardinality isComponent]}]]
              [k (cond-> {}
                   unique                               (assoc :db/unique unique)
                   (some? isComponent)                  (assoc :db/isComponent isComponent)
                   (= :db.cardinality/many cardinality) (assoc :db/cardinality cardinality)
                   (= :db.type/ref valueType)           (assoc :db/valueType valueType))])]
      (->> omni
           (map select-compat)
           (filter (fn [[k v]] (not-empty v)))
           (into {})))))

;; Define all specs, being sure to define all non-keyword (non-dependent) specs first
(defn spec-def
  "Evals s/def for all the specs in the given omni map."
  [omni]
  (doseq [spec-key (sort-by #(-> omni % :omnigen/spec keyword?)
                            (keys omni))]
    (when-let [spec (-> omni spec-key :omnigen/spec)]
      (eval `(s/def ~spec-key ~spec)))))
