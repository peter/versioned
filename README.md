# Versioned

A clojure framework that provides a CMS REST API based on MongoDB. Features include token based user authentication, JSON schema validation, versioning, publishing, relationships, changelog, partial [jsonapi.org](http://jsonapi.org) compliance, Swagger documentation, Heroku deployment, and a model API with before/after callbacks on create/update/delete operations.

The background of this library is that it is a re-implementation and simplification of the
Node.js/Mongodb CMS API that we built to power the Swedish recipe website [köket.se](http://www.koket.se)
in 2015.

## Demo

There is an online demo of this API at [versioned.herokuapp.com](https://versioned.herokuapp.com).

## Maturity

This framework is a work in progress and has not been used in production yet.

## Getting Started Tutorial

First make sure you have [Leiningen/Clojure](http://leiningen.org) and Mongodb installed. This framework is available
via the following Leiningen dependency:

```
[versioned "0.1.0-SNAPSHOT"]
```

Check out [versioned-example](https://github.com/peter/versioned-example) to get a feeling for what a simple app based on this framework might look like.

## Models

Models (i.e. resources, content types) are at the heart of the Versioned framework and they are the blueprints for your
application. Take a look at this example pages model:

```clojure
(ns my-app.models.pages
  (:require [versioned.model-spec :refer [generate-spec]]
            [versioned.model-includes.content-base-model :refer [content-base-spec]]))

(def model-type :pages)

(defn spec [config]
  (generate-spec
    (content-base-spec model-type)
    {
    :type model-type
    :schema {
      :type "object"
      :properties {
        :title {:type "string"}
        :description {:type "string"}
        :widgets_ids {
          :type "array"
          :items {
            :type "integer"
          }
        }
      }
      :additionalProperties false
      :required [:title]
    }
    :relationships {
      :widgets {}
    }
    :indexes [
      {:fields [:title] :unique true}
    ]
  }))
```

The `spec` function is invoked by the framework and should return a map that serves as a specification for the model. The following properties are part of a model specification:

* `:type` - the name of the model in URLs and the Mongodb collection name
* `:schema` - a JSON schema that is used to validate documents before they are saved to the
 database.
* `:callbacks` - functions to invoke `before` or `after` `update`, `create`, or `delete`.
* `:relationships` - associations to other models (the `widgets` relationship above corresponds
 to the `widgets_ids` property)
* `:indexes` - a list of indexes that should be created in Mongodb for the collection
* `:routes` - an optional array of endpoints to expose in the API for the model. The default routes inherited from `content-base-model` are all the REST routes, i.e. [:list :get :create :update :delete]

The `pages` model above "inherits" from the `content-base-model` that provides the following features:

* `id-model` - an integer sequential id field (i.e. like a primary key in a relational database - used instead of the Mongodb `_id` field which is a 24 character hexadecimal UUID)
* `typed-model` - adds a type field to MongoDB documents that is simply the type of the model
* `audited-model` - adds `created_at`, `created_by`, `updated_at`, `updated_by` fields
* `versioned-model` - adds a `version` field that increments on updates and saves each version in a separate MongoDB collection
* `published-model` - adds the fields `published_version`, `publish_at`, and `unpublish_at`. The `published_version` field points out the version that is currently published. If it's not set then the document is not published.
* `validated-spec` - adds a callback that validates the document against the model schema before create and update.
* `routed-model` - sets the `:routes` property of the model to [:list :get :create :update :delete] so that all REST endpoints are exposed via the API

As an example of how the `callbacks` property works, take a look at the callbacks added by `audited-model`:

```clojure
(defn audit-create-callback [doc options]
  (assoc doc :created_at (d/now)))

(defn audit-update-callback [doc options]
  (assoc doc :updated_at (d/now)))

(def audited-callbacks {
  :create {
    :before [audit-create-callback]
  }
  :update {
    :before [audit-update-callback]
  }
})
```

## Creating an Admin User

```
lein repl
(require 'versioned)
(def system (versioned/-main))
(require '[versioned.models.users :as users])
(users/create (:app system) {:name "Admin User" :email "admin@example.com" :password "admin"})
```

## Starting the Server with the Example Models

From the command line:

```
lein run
```

From the REPL:

```
lein repl
(require 'versioned.example.app)
(def system (versioned.example.app/-main))
```

## Running All Tests

```
lein test
```

## Running Unit Tests

```
lein midje
```

## Running API Tests

```
lein run -m api.test-runner
```

## Bulk Import

There is a bulk import API that you can use if you need to load larger amounts of data (i.e. migrate from another CMS):

```
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"model": "widgets", "data": [{"title": {"se": "Latest Movies"}, "published_version": 1}, {"title": {"se": "Latest Series"}}]}' http://localhost:5000/v1/bulk_import
```

## TODO

* Validation
  * Validate association id references before save
  * Validate published_version reference before save
  * unique constraint
  * deal with mongo errors?

* Better compliance with jsonapi.org?

* list endpoint
  * support sort query parameter
  * support query?

* Add first_published_at to published-model

* Scheduler that publishes and unpulishes documents based on publish_at/unpublish_at
