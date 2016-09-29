(ns versioned.util.core
  (:require [clojure.string :as str]
            [clojure.walk]))

(def keywordize-keys clojure.walk/keywordize-keys)

(defn blank?
  "returns true if value is nil or empty"
  [value]
  (cond (nil? value) true
        (and (string? value) (= (count (str/trim value)) 0)) true
        (and (coll? value) (= (count value) 0)) true
        :else false))

; Convert keyword to string.
; Doing (name keyword) will not retain the namespace of the keyword
; http://stackoverflow.com/questions/16594610/what-is-the-right-way-to-convert-a-namespaced-clojure-keyword-to-string
(defn keyword-str [value]
  (if (keyword? value)
    (subs (str value) 1)
    value))
    
(defn present? [value]
  (not (blank? value)))

(defn str-to-regex [s]
  (re-pattern (java.util.regex.Pattern/quote s)))

(defn parse-int
      "idempotent string to integer conversion"
      [string-or-int]
  (if (string? string-or-int) (. Integer parseInt string-or-int) string-or-int))

(defn positive-int? [v]
  (and (integer? v)
       (or (= 0 v) (pos? v))))

(defn safe-parse-int [string-or-int]
  (try
    (parse-int string-or-int)
    (catch java.lang.NumberFormatException e nil)))

(defn valid-int? [value]
  (safe-parse-int value))

(defn boolean? [v]
  (instance? Boolean v))

(defn parse-bool [value]
  (cond
    (contains? #{nil false "0" "f" "false"} value) false
    (contains? #{true "1" "t" "true"} value) true
    :default value))

(defn load-var [path]
  (let [[m f] (str/split path #"/")
        ns-path (symbol m)]
    (require ns-path)
    (ns-resolve (find-ns ns-path) (symbol f))))

(defn round
  "Round a double to the given precision (number of significant digits)"
  [d & {:keys [digits] :or {digits 0}}]
  (let [factor (Math/pow 10 digits)
        result (/ (Math/round (* d factor)) factor)]
    (if (= digits 0)
      (Math/round result)
      result)))

(defn percent [from to & {:keys [digits] :or {digits 0}}]
  (round (* 100 (/ (- to from) from)) :digits digits))

(defn array [value]
  (if (coll? value)
    value
    [value]))

(defn filter-property [docs name value]
  (filter #(= (name %) value) docs))

(defn find-property [docs name value]
  (first (filter-property docs name value)))

(defn split-by-comma [value]
  (if (string? value) (str/split value #",") value))

; Replaces each value v in map with f(v)
; http://stackoverflow.com/questions/1676891/mapping-a-function-on-the-values-of-a-map-in-clojure
(defn map-values [f m]
  (reduce (fn [altered-map [k v]] (assoc altered-map k (f v))) {} m))

; Replaces each value v in map with f(k) where k is the corresponding key
(defn map-key-values [f m]
  (reduce (fn [altered-map [k v]] (assoc altered-map k (f k))) {} m))

(defn deep-map-values
  ([f v opts]
   (let [default-opts {:recurse-if? #(coll? (:value %))
                       :path []}
         opts (merge default-opts opts)
         {:keys [recurse-if? path]} opts
         args {:key (last path) :value v :path path}
         recurse? (recurse-if? args)]
     (cond
       (and (map? v) recurse?)
       (reduce (fn [altered-map [k mv]]
                 (let [new-opts (update-in opts [:path] conj k)]
                   (assoc altered-map k (deep-map-values f mv new-opts))))
               {}
               v)
       (and (coll? v) recurse?)
       (map-indexed (fn [i item]
                      (let [new-opts (update-in opts [:path] conj i)]
                        (deep-map-values f item new-opts)))
                    v)
       :else (f args))))
  ([f v]
   (deep-map-values f v {})))

; Wrap a function in a nil check, i.e. only execute function if value is not nil
(defn maybe [f]
  (fn [value]
    (if (nil? value)
      value
      (f value))))

; See https://github.com/puppetlabs/clj-kitchensink
(defn deep-merge
  "Deeply merges maps so that nested maps are combined rather than replaced.
  For example:
  (deep-merge {:foo {:bar :baz}} {:foo {:fuzz :buzz}})
  ;;=> {:foo {:bar :baz, :fuzz :buzz}}
  ;; contrast with clojure.core/merge
  (merge {:foo {:bar :baz}} {:foo {:fuzz :buzz}})
  ;;=> {:foo {:fuzz :quzz}} ; note how last value for :foo wins"
  [& vs]
  (if (every? map? vs)
    (apply merge-with deep-merge vs)
    (last vs)))

; See http://dev.clojure.org/jira/browse/CLJ-1468
(defn deep-merge-with
  "Like merge-with, but merges maps recursively, appling the given fn
  only when there's a non-map at a particular level.

  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
               {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))

(defn partial-right
  "Takes a function f and fewer than the normal arguments to f, and
 returns a fn that takes a variable number of additional args. When
 called, the returned function calls f with additional args + args."
  ([f] f)
  ([f arg1]
   (fn [& args] (apply f (concat args [arg1]))))
  ([f arg1 arg2]
   (fn [& args] (apply f (concat args [arg1 arg2]))))
  ([f arg1 arg2 arg3]
   (fn [& args] (apply f (concat args [arg1 arg2 arg3]))))
  ([f arg1 arg2 arg3 & more]
   (fn [& args] (apply f (concat args (concat [arg1 arg2 arg3] more))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; compact multimethod
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti compact map?)

(defmethod compact true [map]
  (into {} (remove (comp nil? second) map)))

(defmethod compact false [col]
  (remove nil? col))
