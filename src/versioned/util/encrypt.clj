(ns versioned.util.encrypt
  (:require [crypto.password.scrypt :as password]))

(defn generate [word]
  (password/encrypt word))

(defn check [word encrypted]
  (password/check word encrypted))
