(ns versioned.model-callbacks-test
  (:use midje.sweet)
  (:require [versioned.model-callbacks :as model-callbacks]))

(fact "normalize-callbacks: does not touch callbacks without save"
  (model-callbacks/normalize-callbacks {:create {:before [:foo]} :update {:before [:bar]}}) =>
    {:create {:before [:foo]} :update {:before [:bar]}})

(fact "normalize-callbacks: merges save callbacks with update/create callbacks"
  (model-callbacks/normalize-callbacks {:save {:before [:foo]} :update {:before [:bar]}}) =>
    {:create {:before [:foo]} :update {:before [:foo :bar]}})

(fact "invoke-callbacks: invokes callbacks in order with [doc options] and returns the resulting doc"
  (let [callbacks [
          (fn [doc options]
            (assoc (update-in doc [:callback_trail] concat [:first]) :options-received (= (:app options) "the-app")))
          (fn [doc options]
            (assoc (update-in doc [:callback_trail] concat [:second]) :new-attribute true :title "foobar changed"))
          ]
        doc {:title "foobar"}
        options {:app "the-app"}]
      (model-callbacks/invoke-callbacks callbacks options doc) =>
        {:title "foobar changed" :new-attribute true :options-received true :callback_trail [:first :second]}))

(fact "with-callbacks: wraps a model fn [app model-spec doc] in a function that invokes callbacks before/after"
  (let [model-fn (fn [app model-spec doc] (update-in doc [:callback_trail] concat [:model]))
        action :create
        callbacks {:create {
          :before [(fn [doc options]
                     (let [options-received (and (= (:app options) {:database "the-database"})
                                                 (= (:action options) :create))]
                       (assoc (update-in doc [:callback_trail] concat [:first]) :options-received options-received)))]
          :after  [(fn [doc options]
                     (assoc (update-in doc [:callback_trail] concat [:second]) :new-attribute true :title "foobar changed"))]
          }}
        model-fn-with-callbacks (model-callbacks/with-callbacks model-fn action)
        app {:database "the-database"}
        model-spec {:type :pages :callbacks callbacks}
        doc {:title "foobar"}]
      (model-fn-with-callbacks app model-spec doc) =>
        {:title "foobar changed" :new-attribute true :options-received true :callback_trail [:first :model :second]}))
