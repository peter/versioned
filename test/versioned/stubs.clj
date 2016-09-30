(ns versioned.stubs)

(def app {
  :config {:models {:articles "versioned.example.models.articles/spec"}}
  :models {
    :articles {
      :type :articles
      :schema {
        :type "object"
      }
    }
  }
  :swagger {}
  :routes []
})
