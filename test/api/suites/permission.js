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
            it: "can get a published widget",
            request: "GET /widgets/{{widgets.welcome.id}}?published=1",
            headers: "{{headers.read}}"
          },
          {
            it: "can list published page documents",
            request: "GET /pages?published=1",
            headers: "{{headers.read}}",
          },
          {
            it: "can not list unpublished page documents",
            request: "GET /pages",
            headers: "{{headers.read}}",
            status: 401
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
