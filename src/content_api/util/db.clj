(ns content-api.util.db
  (:import [org.bson.types ObjectId]))

(defn json-friendly [doc]
  (let [friendly-id (.toString (:_id doc))]
    (assoc doc :_id friendly-id)))

(defn mongo-friendly [doc]
  (if (string? (:_id doc))
    (assoc doc :_id (ObjectId. (:_id doc)))
    doc))

(defn mongo-map [fields]
  (apply array-map (interleave fields (repeat 1))))

(defn valid-object-id? [object-id]
  (re-matches #"^[0-9a-fA-F]{24}$" object-id))
