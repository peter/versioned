(ns versioned.stubs)

(def app {
  :config {:models {:articles "versioned.example.models.articles/spec"}}
  :models (atom {
    :articles {
      :type :articles
      :schema {
        :type "object"
      }
    }
  })
  :swagger (atom {})
  :routes (atom [])
})
