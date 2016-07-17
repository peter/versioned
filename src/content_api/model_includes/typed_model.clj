(ns content-api.model-includes.typed-model)

(defn type-callback [doc options]
  (let [type (get-in options [:model-spec :type])]
    (assoc doc :type (name type))))

(def typed-schema {
  :type "object"
  :properties {
    :type {:type "string"}
  }
  :required [:type]
})

(def typed-callbacks {
    :save {
      :before [type-callback]
    }
})

(defn typed-spec [& options] {
  :schema typed-schema
  :callbacks typed-callbacks
})
