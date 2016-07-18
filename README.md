# Content API

A clojure library that provides a CMS REST API based on MongoDB. Features include token based user authentication, JSON schema validation, versioning, publishing, relationships, changelog,
and a model API with before/after callbacks on create/update/delete operations.

The background of this library is that it is a re-implementation and simplification of the
Node.js/Mongodb CMS API that we built to power the Swedish recipe website [köket.se](http://www.koket.se)
in 2015.

## TODO

* Put locales/sites in config

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
