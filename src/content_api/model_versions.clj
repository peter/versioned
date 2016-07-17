(ns content-api.model-versions
  (:require [content-api.util.core :as u]
            [content-api.model-support :as model-support]))

(defn versioned-attribute? [attribute-schema]
  (get attribute-schema :versioned true))

(defn versioned-attributes [schema]
  (filter #(versioned-attribute? (% (:properties schema)))
          (keys (:properties schema))))

(defn unversioned-attributes [schema]
  (clojure.set/difference (set (keys (:properties schema)))
                          (set (versioned-attributes schema))))

(defn versioned-coll [model-spec]
  (keyword (str (name (model-support/coll model-spec)) "_versions")))

(defn versioned-id-query [model-spec id version]
  (merge (model-support/id-query model-spec id)
         {:version version}))

(defn select-version [doc version-param published?]
  (if published?
    (:published_version doc)
    (and version-param (u/safe-parse-int version-param))))

(defn apply-version [model-spec doc versioned-doc]
  (and versioned-doc (merge versioned-doc
                            (select-keys doc
                                         (unversioned-attributes (:schema model-spec))))))

(defn published-model? [app model]
  (and model (get-in app [:models model :schema :properties :published_version])))
