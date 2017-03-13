# Versioned

A clojure framework that provides a CMS REST API based on MongoDB. Features include token based user authentication, JSON schema validation, versioning, publishing, relationships, changelog, partial [jsonapi.org](http://jsonapi.org) compliance, [Swagger](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md) documentation, Heroku deployment, and a model API with before/after callbacks on create/update/delete operations.

The background of this library is that it is a re-implementation, generalization, and simplification of the
Node.js/Mongodb CMS API that we built to power the Swedish recipe website [köket.se](http://www.koket.se)
in 2015.

## Demo and API Doc

There is an online example application with Swagger API documentation at [versioned.herokuapp.com](https://versioned.herokuapp.com).

## Maturity

This framework is used in production but should not be considered mature yet.

## Example App and Getting Started Tutorial

First make sure you have [Leiningen/Clojure](http://leiningen.org) and Mongodb installed. This framework is available
via the following Leiningen dependency:

[![Clojars Project](http://clojars.org/versioned/latest-version.svg)](http://clojars.org/versioned)

Check out [example/app.clj](src/versioned/example/app.clj) to get a feeling for what a simple app based on this framework might look like. A similar example app is also available in a separate repo called [versioned-example](https://github.com/peter/versioned-example) and you can use that as boilerplate to get started.

Let's try running the example app embedded in this library. Check out the code and create an admin user via the REPL:

```
git clone git@github.com:peter/versioned.git
cd versioned
lein repl
(require 'versioned)
(def system (versioned.example.app/-main :start-web false))
(require '[versioned.models.users :as users])
(users/create (:app system) {:name "Admin User" :email "admin@example.com" :password "admin" :permission "write"})
exit
```

Start the server from the command line:

```
lein run
```

The server can also be started from the REPL:

```
lein repl
(require 'versioned.example.app)
(def system (versioned.example.app/-main))
```

In a different terminal, log in:

```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"email": "admin@example.com", "password": "admin"}' http://localhost:5000/v1/login

export TOKEN=<auth token in header response above>
```

Basic CRUD workflow:

```bash
# create
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"data": {"attributes": {"title": {"se": "My Section"}, "slug": {"se": "my-section"}}}}' http://localhost:5000/v1/sections

# get
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/sections/1

# list
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/sections

# update
curl -i -X PUT -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"data": {"attributes": {"title": {"se": "My Section EDIT"}}}}' http://localhost:5000/v1/sections/1

# delete
curl -i -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/sections/1
```

Now, let's look at versioning, associations, and publishing. Create two widgets and a page:

```bash
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"data": {"attributes": {"title": {"se": "Latest Movies"}, "published_version": 1}}}' http://localhost:5000/v1/widgets

curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"data": {"attributes": {"title": {"se": "Latest Series"}}}}' http://localhost:5000/v1/widgets

curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"data": {"attributes": {"title": {"se": "Start Page"}, "widgets_ids": [1, 2], "published_version": 1}}}' http://localhost:5000/v1/pages
```

The first widget and the page are published since the `published_version` is set but the second widget is not. Now we can fetch the page with its associations:

```bash
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/pages/1?relationships=1
```

The response looks something like:

```json
{
  "data" : {
    "id" : "1",
    "type" : "pages",
    "attributes" : {
      "version" : 1,
      "created_at" : "2016-07-18T08:36:10.887+02:00",
      "type" : "pages",
      "id" : 1,
      "created_by" : "admin@example.com",
      "widgets_ids" : [ 1, 2 ],
      "title" : {
        "se" : "Start Page"
      },
      "published_version" : 1,
      "_id" : "578c78daf2b4a45bcddb65a1"
    },
    "relationships" : {
      "versions" : {
        "data" : [ {
          "id" : "1",
          "type" : "pages",
          "attributes" : {
            "created_by" : "admin@example.com",
            "created_at" : "2016-07-18T08:36:10.900+02:00",
            "version" : 1,
            "widgets_ids" : [ 1, 2 ],
            "id" : 1,
            "title" : {
              "se" : "Start Page"
            },
            "type" : "pages",
            "published_version" : 1,
            "_id" : "578c78daf2b4a45bcddb65a2"
          }
        } ]
      },
      "widgets" : {
        "data" : [ {
          "id" : "1",
          "type" : "widgets",
          "attributes" : {
            "version" : 1,
            "created_at" : "2016-07-18T08:35:02.281+02:00",
            "type" : "widgets",
            "id" : 1,
            "created_by" : "admin@example.com",
            "title" : {
              "se" : "Latest Movies"
            },
            "published_version" : 1,
            "_id" : "578c7896f2b4a45bcddb659b"
          }
        }, {
          "id" : "2",
          "type" : "widgets",
          "attributes" : {
            "version" : 1,
            "created_at" : "2016-07-18T08:35:31.708+02:00",
            "type" : "widgets",
            "id" : 2,
            "created_by" : "admin@example.com",
            "title" : {
              "se" : "Latest Series"
            },
            "_id" : "578c78b3f2b4a45bcddb659e"
          }
        } ]
      }
    }
  }
}
```

Notice how the page has a single version and how it is associated with two widgets, only the first of which has a published version.
Now, if we ask for the published version of the page (relevant to the end-user/public facing website) we don't get the version history
and we only get the first widget:

```bash
curl -i -H "Authorization: Bearer $TOKEN" 'http://localhost:5000/v1/pages/1?relationships=1&published=1'
```

If the page hadn't been published we would have gotten a 404.

In addition to the version history there is a `changelog` collection in Mongodb with a log of all write operations performed via the API:

```bash
curl -i -H "Authorization: Bearer $TOKEN" 'http://localhost:5000/v1/changelog'
```

Here is an example entry from the update above:

```json
{
  "action": "update",
  "errors": null,
  "doc": {
    "slug": {
      "se": "my-section"
    },
    "type": "sections",
    "title": {
      "se": "My Section EDIT"
    },
    "updated_at": "2016-07-18T06:29:50.142Z",
    "id": 1,
    "updated_by": "admin@example.com",
    "version": 2,
    "created_by": "admin@example.com",
    "created_at": "2016-07-18T06:29:34.924Z"
  },
  "changes": {
    "title": {
      "from": {
        "se": "My Section"
      },
      "to": {
        "se": "My Section EDIT"
      }
    }
  },
  "created_by": "admin@example.com",
  "created_at": "2016-07-18T06:29:50.167Z"
}
```

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
 database. For reading up on JSON schema I recommend [Understanding JSON Schema](https://spacetelescope.github.io/understanding-json-schema/UnderstandingJSONSchema.pdf).
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

## Running Library Tests

To run both unit and API (HTTP level) tests, do:

```
lein test-al
```

The `test-all` task runs the `test` (unit test) and `test-api` tasks.

## Import

There is a bulk import API that you can use if you need to load larger amounts of data (i.e. migrate from another CMS):

```
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"model": "widgets", "data": [{"title": {"se": "Latest Movies"}, "published_version": 1}, {"title": {"se": "Latest Series"}}]}' http://localhost:5000/v1/import_initial
```

There are also two endpoints for syncing - `import_sync/delete` and `import_sync/upsert`.

## TODO

* API tests for import API, especially the sync part

* More API tests, i.e. related to publishing

* Validation
  * Validate association id references before save
  * Validate published_version reference before save

* Handle mongodb WriteConcernException as a validation error? Use mongo error codes? See https://api.mongodb.com/java/3.0/com/mongodb/DuplicateKeyException.html

* Logger should take config as first argument instead of app?

* Use [clojure.tools.logging](https://github.com/clojure/tools.logging/blob/master/README.md)

* The changelog mechanism is fragile in how it interacts with callbacks and the :existing-doc meta field since if any of the callbacks
  do not retain the meta data then it breaks.

* params-parser API test

* Move parse functions to their own namespace, safe-coerce-value

* Get reload to work again

* Should not allow both version and published params in get endpoint

* git rm checkouts/monger as soon as Clojure 1.9 compatible version is available (https://github.com/michaelklishin/monger/issues/142)

* Better compliance with jsonapi.org?

* Add first_published_at to published-model

* Scheduler that publishes and unpublishes documents based on publish_at/unpublish_at
