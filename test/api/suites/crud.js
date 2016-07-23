"use strict";

var assert = require('assert');

var page = {
  "title": {
    "se": "Testsida"
  },
  "slug": {
    "se": "testsida"
  }
};

module.exports = {
  suite: {
    name: "CRUD",
    tests: [
      {
        name: "create_get_list_update_delete",
        description: "You can create a page document, get and list it, update its name, and then delete it",
        api_calls: [
          {
            it: "can create a page document",
            request: "POST /pages",
            params: {pages: page},
            assert: {
              select: "body.data",
              equal_keys: {
                title: page.title
              },
              schema: "{{schema.pages}}"
            },
            save: {
              "page_id": "body.data.id"
            }
          },
          {
            it: "can get the created page document",
            request: "GET /pages/{{page_id}}",
            assert: {
              select: "body.data.attributes",
              equal_keys: {
                id: "{{page_id}}",
                title: page.title
              },
              schema: "{{schema.pages}}"
            }
          },
          {
            it: "can list page documents",
            request: "GET /pages",
            assert: {
              select: "body.data",
              contains_keys: {
                id: "{{page_id}}",
                title: page.title
              }
            }
          },
          {
            it: "can update name of page document",
            request: "PUT /pages/{{page_id}}",
            params: {
              pages: {title: {se: "Testsida EDIT"}}
            }
          },
          {
            it: "can get new name of page document",
            request: "GET /pages/{{page_id}}",
            assert: {
              select: "body.data.attributes",
              equal_keys: {
                id: "{{page_id}}",
                title: {se: "Testsida EDIT"}
              }
            }
          },
          {
            it: "can delete page document",
            request: "DELETE /pages/{{page_id}}"
          },
          {
            it: "can verify that page document was deleted",
            request: "GET /pages/{{page_id}}",
            status: 404
          }
        ]
      }
    ]
  }
};
