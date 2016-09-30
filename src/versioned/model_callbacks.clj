(ns versioned.model-callbacks
  (:require [versioned.util.core :as u]
            [schema.core :as s]
            [versioned.types :refer [PosInt
                                     Map
                                     Doc
                                     ModelWriteFn
                                     Callback
                                     CallbackMap
                                     CallbackFunction
                                     CallbackOptions
                                     CallbackAction
                                     Callbacks]]
            [versioned.model-validations :refer [model-errors]]))

(s/defn merge-callbacks :- Callbacks
  [& callbacks :- [Callbacks]]
  (apply u/deep-merge-with concat callbacks))

(def sort-keys [:first :middle :last])

(s/defn callback-map :- CallbackMap
  [callback :- Callback]
  (if (map? callback) callback {:fn callback}))

(s/defn callback-fn :- CallbackFunction
  [callback :- Callback]
  (if (map? callback) (:fn callback) callback))

(s/defn sort-index :- PosInt
  [callback :- Callback]
  (let [sort-key (get (callback-map callback) :sort :middle)]
    (.indexOf sort-keys sort-key)))

(s/defn sort-callbacks :- Callbacks
  [callbacks :- Callbacks]
  (u/deep-map-values #(sort-by sort-index (:value %))
                     callbacks
                     {:recurse-if? #(map? (:value %))}))

(s/defn save-callbacks :- Callbacks
  [callbacks :- Callbacks]
  {:update (:save callbacks) :create (:save callbacks)})

(s/defn normalize-save :- Callbacks
  [callbacks :- Callbacks]
  (if (:save callbacks)
    (merge-callbacks (save-callbacks callbacks) (dissoc callbacks :save))
    callbacks))

(s/defn normalize-callbacks :- Callbacks
  [callbacks :- Callbacks]
  (->> callbacks
      (normalize-save)))

(s/defn composed-callbacks :- CallbackFunction
  [callbacks :- [Callback]
   options :- CallbackOptions]
  (apply comp (map #(u/partial-right % options) (reverse (map callback-fn callbacks)))))

(s/defn invoke-callbacks :- Doc
  [callbacks :- [Callback]
   options :- CallbackOptions
   doc :- Doc]
  ((composed-callbacks callbacks options) doc))

(s/defn with-callbacks :- ModelWriteFn
  [model-fn :- ModelWriteFn
   action :- CallbackAction]
  (fn [app model-spec doc]
    (let [before-callbacks (get-in model-spec [:callbacks action :before] [])
          after-callbacks (get-in model-spec [:callbacks action :after] [])
          options {
            :app app
            :config (:config app)
            :database (:database app)
            :action action
            :model-spec model-spec
            :schema (:schema model-spec)}]
        ; TODO: capture this chaining pattern with an abort condition in a function/macro?
        (let [doc (invoke-callbacks before-callbacks options doc)]
          (if-not (model-errors doc)
            (let [doc (model-fn app model-spec doc)]
              (if-not (model-errors doc)
                (invoke-callbacks after-callbacks options doc)
                doc))
            doc)))))
