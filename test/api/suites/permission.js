"use strict";

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
    name: "permission",
    tests: [
      {
        name: "read-write-permission",
        description: "Users with read permission can read data but not write data",
        api_calls: [
          {
            it: "can get a page document",
            request: "GET /pages/{{pages.start.id}}",
            headers: "{{headers.read}}",
            assert: {
              select: "body.data.attributes",
              equal_keys: {
                id: "{{pages.start.id}}",
                title: "{{pages.start.title}}",
              },
              schema: "{{schema.pages}}"
            }
          },
          {
            it: "can list page documents",
            request: "GET /pages",
            headers: "{{headers.read}}",
          },
          {
            it: "can not create a page document",
            request: "POST /pages",
            headers: "{{headers.read}}",
            params: {data: {attributes: page}},
            status: 401
          },
          {
            it: "can not update page document",
            request: "PUT /pages/{{pages.start.id}}",
            headers: "{{headers.read}}",
            params: {
              data: {attributes: {title: {se: "Testsida EDIT"}}}
            },
            status: 401
          },
          {
            it: "can not delete page document",
            request: "DELETE /pages/{{pages.start.id}}",
            headers: "{{headers.read}}",
            status: 401
          }
        ]
      }
    ]
  }
};
