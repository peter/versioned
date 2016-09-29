(ns versioned.db-api
  (:refer-clojure :exclude [find update delete count])
  (:require [versioned.util.db :refer [json-friendly mongo-friendly mongo-map]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.joda-time]
            [schema.core :as s]
            [versioned.schema :refer [Nil Database DB-Conn DB-IndexOptions Map PosInt WriteResult]]
            [com.stuartsierra.component :as component])
  (:import [org.bson.types ObjectId]
           com.mongodb.DB))

; Mongo API doc: http://clojuremongodb.info/articles/getting_started.html

(s/defn connect :- Database
  [uri :- String]
  (mg/connect-via-uri uri))

(s/defn disconnect :- Nil
  [conn :- DB-Conn]
  (mg/disconnect conn))

(s/defn ensure-index :- Nil
  [database :- Database
   coll :- s/Keyword
   fields :- [s/Keyword]
   options :- DB-IndexOptions]
  (mc/ensure-index (:db database) coll (mongo-map fields) options))

(s/defn find :- [Map]
  ([database :- Database
    coll :- s/Keyword
    query :- Map
    opts :- Map]
    (let [default-opts {:page 1 :per-page 100 :sort {} :fields []}
          opts (merge default-opts opts)]
      (map json-friendly (mq/with-collection (:db database) (name coll)
                                             (mq/find (mongo-friendly query))
                                             (mq/fields (:fields opts))
                                             (mq/paginate :page (:page opts) :per-page (:per-page opts))
                                             (mq/sort (:sort opts))))))
  ([database :- Database
    coll :- s/Keyword
    query :- Map]
    (find database coll query {})))

(s/defn find-one :- (s/maybe Map)
  [database :- Database
   coll :- s/Keyword
   query :- Map]
  (first (find database coll query)))

(s/defn count :- PosInt
  [database :- Database
   coll :- s/Keyword
   query :- Map]
  (mc/count (:db database) coll query))

(s/defn create :- {:doc Map :result WriteResult}
  [database :- Database
   coll :- s/Keyword
   doc :- Map]
  (let [created-doc (assoc doc :_id (ObjectId.))
        result (mc/insert (:db database) coll created-doc)]
    {:doc (json-friendly created-doc) :result result}))

; For partial update - use: {:$set {:foo "bar"}}
(s/defn update :- {:doc Map :result WriteResult}
  [database :- Database
   coll :- s/Keyword
   query :- Map
   doc :- Map]
  (let [result (mc/update (:db database) coll (mongo-friendly query) (mongo-friendly doc))
        updated-doc (find-one database coll query)]
    {:doc updated-doc :result result}))

(s/defn delete :- WriteResult
  [database :- Database
   coll :- s/Keyword
   query :- Map]
  (mc/remove (:db database) coll (mongo-friendly query)))
