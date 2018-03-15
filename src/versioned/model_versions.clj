(ns versioned.model-versions
  (:require [versioned.util.core :as u]
            [clojure.set :refer [difference]]
            [versioned.model-init :refer [get-in-model]]
            [versioned.model-support :as model-support]))

(defn versioned-attribute? [attribute-schema]
  (get-in attribute-schema [:x-meta :versioned] true))

(defn versioned-attributes [schema]
  (filter #(versioned-attribute? (% (:properties schema)))
          (keys (:properties schema))))

(defn unversioned-attributes [schema]
  (difference (set (keys (:properties schema)))
              (set (versioned-attributes schema))))

(defn versioned-coll [model-spec]
  (keyword (str (name (model-support/coll model-spec)) "_versions")))

(defn versioned-id-query [model-spec id version version-token]
  (merge (model-support/id-query model-spec id)
         (u/compact {:version version :version_token version-token})))

(defn select-version [doc version-param published?]
  (if published?
    (:published_version doc)
    (and version-param (u/safe-parse-int version-param))))

(defn apply-version [model-spec doc versioned-doc]
  (if (and versioned-doc (not= (:version doc) (:version versioned-doc)))
    (and versioned-doc (merge versioned-doc
                            (select-keys doc
                                         (unversioned-attributes (:schema model-spec)))))
    doc))

(defn published-model? [app model]
  (and model (get-in-model app [model :schema :properties :published_version])))
