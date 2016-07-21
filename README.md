# Content API

A clojure library that provides a CMS REST API based on MongoDB. Features include token based user authentication, JSON schema validation, versioning, publishing, relationships, changelog,
and a model API with before/after callbacks on create/update/delete operations.

The background of this library is that it is a re-implementation and simplification of the
Node.js/Mongodb CMS API that we built to power the Swedish recipe website [köket.se](http://www.koket.se)
in 2015.

## Getting Started Tutorial

First make sure you have [Leiningen/Clojure](http://leiningen.org) and Mongodb installed.

There is an [example app](https://github.com/peter/content-api-example) that you can use as a starting point or you can proceed by trying out this library directly with the instructions below.

Get the source:

```bash
git clone git@github.com:peter/content-api.git
cd content-api
```

Start the system in the REPL with a few example models:

```
lein repl
(def models {
  :sections "content-api.example-models.sections/spec"
  :pages "content-api.example-models.pages/spec"
  :widgets "content-api.example-models.widgets/spec"})
(def sites ["se" "no" "dk" "fi"])
(require 'content-api)
(def system (content-api/-main :models models :sites sites :locales sites))
```

Create an admin user:

```
(require '[content-api.models.users :as users])
(users/create (:app system) {:name "Admin User" :email "admin@example.com" :password "admin"})
```

Now, in a different terminal, log in:

```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"email": "admin@example.com", "password": "admin"}' http://localhost:5000/v1/login

export TOKEN=<token in header response above>
```

Basic CRUD workflow:

```bash
# create
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"sections": {"title": {"se": "My Section"}, "slug": {"se": "my-section"}}}' http://localhost:5000/v1/sections

# get
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/sections/1

# list
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/sections

# update
curl -i -X PUT -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"sections": {"title": {"se": "My Section EDIT"}}}' http://localhost:5000/v1/sections/1

# delete
curl -i -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/sections/1
```

## Starting the Server

```
lein run
```

## The REPL

```
lein repl
```

## Running Tests

```
lein midje
```

## Bulk Import

There is a bulk import API that you can use if you need to load larger amounts of data (i.e. migrate from another CMS):

```
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"model": "widgets", "data": [{"title": {"se": "Latest Movies"}, "published_version": 1}, {"title": {"se": "Latest Series"}}]}' http://localhost:5000/v1/bulk_import
```

## TODO

* Put all custom properties in the JSON schema under a meta property?
  (def custom-property-keys #{:api_readable :api_writable :versioned :translated})

* Validation
  * Validate association id references before save
  * Validate published_version reference before save

* list endpoint
  * Default sort order id desc
  * support sort query parameter
  * support query?

* Use Swagger: https://github.com/metosin/ring-swagger

* Add first_published_at to published-model

* finish API tests (under api-test)

* Put all model specs in the app object. Memoize model-spec lookup

* get endpoint
  * which fields to include (cms needs more fields than www, compare ommit/disabled in contentful CMS)

* validation
  * unique constraint
  * deal with mongo errors?

* Scheduler that publishes and unpulishes documents based on publish_at/unpublish_at

* comply more with jsonapi.org
