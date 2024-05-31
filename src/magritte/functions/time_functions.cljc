(ns magritte.functions.time-functions
  (:require [magritte.utils :as utils]
            [clojure.string :as str]))

(def ^:private time-functions
  {:day            [string?] ;;	Extracts the day as a number from a datetime
   :floor          [string? keyword?] ;;	Rounds a datetime down by a specific duration
   :format         [string? string?] ;;	Outputs a datetime according to a specific format
   :group          [string? string?] ;;	Groups a datetime by a particular time interval
   :hour           [string?] ;;	Extracts the hour as a number from a datetime
   :max            [vector?] ;;	Finds the most recent datetime in an array
   :micros         [string?] ;;Since 1.1.0	Extracts the microseconds as a number from a datatime
   :millis         [string?] ;;Since 1.1.0	Extracts the milliseconds as a number from a datatime
   :min            [vector?] ;;	Finds the least recent datetime in an array
   :minute         [string?] ;;	Extracts the minutes as a number from a datetime
   :month          [string?] ;;	Extracts the month as a number from a datetime
   :nano           [string?] ;;	Returns the number of nanoseconds since the UNIX epoch
   :now            [] ;;	Returns the current datetime
   :round          [string? keyword?] ;;	Rounds a datetime to the nearest multiple of a specific duration
   :second         [string?] ;;	Extracts the second as a number from a datetime
   :timezone       [] ;;	Returns the current local timezone offset in hours
   :unix           [string?] ;;	Returns the number of seconds since the UNIX epoch
   :wday           [string?] ;;	Extracts the week day as a number from a datetime
   :week           [string?] ;;	Extracts the week as a number from a datetime
   :yday           [string?] ;;	Extracts the yday as a number from a datetime
   :year           [string?] ;;	Extracts the year as a number from a datetime
   [:from :micros] [number?] ;;Since 1.1.0	Calculates a datetime based on the microseconds since January 1, 1970 0:00:00 UTC.
   [:from :millis] [number?] ;;	Calculates a datetime based on the milliseconds since January 1, 1970 0:00:00 UTC.
   [:from :nanos]  [number?] ;;Since 1.1.0	Calculates a datetime based on the nanoseconds since January 1, 1970 0:00:00 UTC.
   [:from :secs]   [number?] ;;	Calculates a datetime based on the seconds since January 1, 1970 0:00:00 UTC.
   [:from :unix]   [number?] ;;	Calculates a datetime based on the seconds since January 1, 1970 0:00:00 UTC.
   })
(defn- args-valid? [validators args]
  (if (>= (count validators) (count args))
    (every? identity (map (fn [v a] (v a)) validators args))
    false))

(defn- matched-fn [function]
  (cond
    (keyword? function) (utils/kebab->snake_name function)
    (vector? function) (str/join "::" (map utils/kebab->snake_name function))))

(defn time-fn [function & args]
  (let [validator (get time-functions function)]
    (if validator
      (if (args-valid? validator args)
        (str "array::" (matched-fn function) "(" (str/join ", " (map utils/to-valid-str args)) ")")
        (throw (ex-info (str "Invalid arguments for time function: " function) {})))
      (throw (ex-info (str "Unknown time function: " function) {})))))
