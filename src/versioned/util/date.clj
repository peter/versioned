(ns versioned.util.date
  (:require [clojure.string :as str]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [versioned.util.core :as u]))

(defn now []
  (l/local-now))

(def year t/year)
(def month t/month)
(def day t/day)

(defn seconds-ago [seconds]
  (t/minus (now) (t/seconds seconds)))

(defn before? [date1 date2]
  (t/before? date1 date2))

(defn parse-date
      "Parse date string like \"2015-01-01\", return a org.joda.time.DateTime
       This function is idempotent so that an already parsed date will pass through untouched."
      [date-or-string]
  (if (string? date-or-string) (f/parse (f/formatters :date) date-or-string) date-or-string))

; JSON date from JavaScript (ISO 8601): "2015-08-25T10:51:59.076Z"
(defn parse-datetime [date-string]
  (f/parse (f/formatters :date-time) date-string))

(defn format-date
      "Generate date string like \"2015-01-01\""
      [date & {:keys [format] :or {format :date}}]
      (f/unparse (f/formatters format) (parse-date date)))

(defn days-ago
      "Return how many days ago a org.joda.time.DateTime is"
      [date-or-string]
  (let [date (parse-date date-or-string)]
    (t/in-days (t/interval date (l/local-now)))))

(defn calculate-date [days-ago & {:keys [from] :or {from (l/local-now)}}]
  (t/minus from (t/days days-ago)))

(defn yesterday []
  (calculate-date 1))

(defn date-int [date-or-string]
  (let [date (parse-date date-or-string)
        date-string (str/replace (format-date date) #"-" "")]
      (u/parse-int date-string)))

(def pretty-months {
  1 "januari"
  2 "februari"
  3 "mars"
  4 "april"
  5 "maj"
  6 "juni"
  7 "juli"
  8 "augusti"
  9 "september"
  10 "oktober"
  11 "november"
  12 "december"})

(defn weekday
  ([] (weekday (l/local-now)))
  ([date] (t/day-of-week (parse-date date))))

(def pretty-weekdays {
  1 "måndag"
  2 "tisdag"
  3 "onsdag"
  4 "torsdag"
  5 "fredag"
  6 "lördag"
  7 "söndag"})

(defn pretty-weekday [weekday]
    (get pretty-weekdays weekday))
