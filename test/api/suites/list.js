"use strict";

module.exports = {
  suite: {
    name: "list",
    tests: [
      {
        name: "query",
        description: "You can use a query parameter to filter out documents returned by list endpoint",
        api_calls: [
          {
            it: "can filter documents by single id",
            request: "GET /pages",
            params: {
              q: "id:1"
            },
            assert: [
              {
                select: "body.data",
                size: 1
              },
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: "{{pages.start.title}}"
                }
              }
            ]
          },
          {
            it: "can filter documents by multiple ids",
            request: "GET /pages",
            params: {
              q: "id:1,2"
            },
            assert: [
              {
                select: "body.data",
                size: 2
              },
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: "{{pages.about.title}}"
                }
              },
              {
                select: "body.data.1.attributes",
                equal_keys: {
                  title: "{{pages.start.title}}"
                }
              }
            ]
          },
          {
            it: "can filter documents by multiple ids and slug",
            request: "GET /pages?q=id:1,2&q=slug.se:{{pages.start.slug.se}}",
            assert: [
              {
                select: "body.data",
                size: 1
              },
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: "{{pages.start.title}}"
                }
              }
            ]
          },
          {
            it: "can filter documents by multiple ids and slug with q[0], q[1] params",
            request: "GET /pages?q[0]=id:1,2&q[1]=slug.se:{{pages.start.slug.se}}",
            assert: [
              {
                select: "body.data",
                size: 1
              },
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: "{{pages.start.title}}"
                }
              }
            ]
          },
          {
            it: "can filter documents by multiple slugs",
            request: "GET /pages?q=|:slug.se:{{pages.start.slug.se}}|{{pages.about.slug.se}}",
            assert: [
              {
                select: "body.data",
                size: 2
              },
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: "{{pages.about.title}}"
                }
              },
              {
                select: "body.data.1.attributes",
                equal_keys: {
                  title: "{{pages.start.title}}"
                }
              }
            ]
          },
          {
            it: "does not accept invalid query",
            request: "GET /pages?q=this-is-not-valid",
            status: 422
          },
          {
            it: "does not accept invalid multiple queries",
            request: "GET /pages?q=this-is-not-valid&q=also-not-valid",
            status: 422
          }
        ]
      }
    ]
  }
};
