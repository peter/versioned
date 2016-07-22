(ns content-api.util.file
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn new-file [path-or-file]
  (if (string? path-or-file)
      (io/file path-or-file)
      path-or-file))

(defn basename [path-or-file]
  (.getName (new-file path-or-file)))

(defn ls [dir & {:keys [ext] :or {ext ""}}]
  (let [dir-name (basename dir)
        files (file-seq (new-file dir))
        names (map basename files)
        name-predicate #(and (not= dir-name %) (str/ends-with? % ext))
        make-path #(str dir "/" %)]
    (->> names
         (filter name-predicate)
         (map make-path))))
