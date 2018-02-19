"use strict";

const crypto = require('crypto')

function digest() {
  const time = (new Date()).valueOf().toString()
  const random = Math.random().toString()
  return crypto.createHash('sha1').update(time + random).digest("hex")
}

const pageDigest = digest()
const page = {
  "title": {
    "se": `Testsida ${pageDigest}`
  },
  "slug": {
    "se": `testsida-${pageDigest}`
  }
};

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
      },

      {
        name: "published",
        description: "You can use a published=1 query parameter to get only published docs",
        api_calls: [
          {
            it: "can create an unpublished page",
            request: "POST /pages",
            params: {data: {attributes: page}},
            save: {
              "page_id": "body.data.attributes.id"
            }
          },
          {
            it: "can list the unpublished page",
            request: "GET /pages",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: page.title
                }
              }
            ]
          },
          {
            it: "can list only published pages and then the unpublished page is not there",
            request: "GET /pages?published=1",
            assert: [
              {
                select: "body.data.0.attributes",
                not_equal_keys: {
                  title: page.title
                }
              }
            ]
          },
          {
            it: "can publish the page",
            request: "PUT /pages/{{page_id}}",
            params: {
              data: {attributes: {published_version: 1}}
            }
          },
          {
            it: "can list only published pages and the published page is now there",
            request: "GET /pages?published=1",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: page.title
                }
              }
            ]
          },
          {
            it: "can update the page",
            request: "PUT /pages/{{page_id}}",
            params: {
              data: {attributes: {title: {se: (page.title.se + ' EDIT')}}}
            }
          },
          {
            it: "can list only published pages and the latest edit is not visible since it's not published",
            request: "GET /pages?published=1",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: page.title
                }
              }
            ]
          },
          {
            it: "can publish the updated version of the page",
            request: "PUT /pages/{{page_id}}",
            params: {
              data: {attributes: {published_version: 2}}
            }
          },
          {
            it: "can list only published pages and the latest edit is now visible as it's been published",
            request: "GET /pages?published=1",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  title: {se: (page.title.se + ' EDIT')}
                }
              }
            ]
          },
        ]
      }
    ]
  }
};
