(ns versioned.model-callbacks
  (:require [versioned.util.core :as u]
            [versioned.model-validations :refer [model-errors]]))

(defn merge-callbacks [& callbacks]
  (apply u/deep-merge-with concat callbacks))

(def sort-keys [:first :middle :last])

(defn- callback-map [callback]
  (if (map? callback) callback {:fn callback}))

(defn- callback-fn [callback]
  (if (map? callback) (:fn callback) callback))

(defn sort-index [v]
  (let [sort-key (get (callback-map v) :sort :middle)]
    (.indexOf sort-keys sort-key)))

(defn sort-callbacks [callbacks]
  (u/deep-map-values #(sort-by sort-index %) callbacks {:recurse-on-coll? false}))

(defn- save-callbacks [callbacks]
  {:update (:save callbacks) :create (:save callbacks)})

(defn- normalize-save [callbacks]
  (if (:save callbacks)
    (merge-callbacks (save-callbacks callbacks) (dissoc callbacks :save))
    callbacks))

(defn normalize-callbacks [callbacks]
  (->> callbacks
      (normalize-save)))

(defn composed-callbacks [callbacks options]
  (apply comp (map #(u/partial-right % options) (reverse (map callback-fn callbacks)))))

(defn invoke-callbacks [callbacks options doc]
  ((composed-callbacks callbacks options) doc))

(defn with-callbacks [model-fn action]
  (fn [app model-spec doc]
    (let [before-callbacks (get-in model-spec [:callbacks action :before] [])
          after-callbacks (get-in model-spec [:callbacks action :after] [])
          options {:app app :database (:database app) :action action :model-spec model-spec :schema (:schema model-spec)}]
        ; TODO: capture this chaining pattern with an abort condition in a function/macro?
        (let [doc (invoke-callbacks before-callbacks options doc)]
          (if-not (model-errors doc)
            (let [doc (model-fn app model-spec doc)]
              (if-not (model-errors doc)
                (invoke-callbacks after-callbacks options doc)
                doc))
            doc)))))
