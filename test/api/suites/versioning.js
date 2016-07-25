"use strict";

var page = {
  "title": {
    "se": "Versions page"
  },
  "slug": {
    "se": "versions-page"
  },
  "widgets_ids": [1, 2]
};

module.exports = {
  suite: {
    name: "versioning",
    tests: [
      {
        name: "versioning of a page",
        api_calls: [
          {
            it: "can create a page document with two widgets",
            request: "POST /pages",
            params: {data: {attributes: page}},
            save: {
              "page_id": "body.data.attributes.id"
            }
          },
          {
            it: "can get the created page which has a single version",
            request: "GET /pages/{{page_id}}?relationships=1",
            assert: [
              {
                select: "body.data.relationships.versions.data",
                size: 1
              },
              {
                select: "body.data.relationships.versions.data.0.attributes",
                equal_keys: {
                  id: "{{page_id}}",
                  version: 1,
                  title: page.title,
                  slug: page.slug,
                  widgets_ids: page.widgets_ids
                }
              }
            ]
          },
          {
            it: "can update the page to create a second version",
            request: "PUT /pages/{{page_id}}",
            params: {
              data: {attributes: {
                title: {se: "Versions page EDIT"},
                widgets_ids: [1]
              }}
            },
            assert: {
              select: "body.data.attributes",
              equal_keys: {
                id: "{{page_id}}",
                title: {se: "Versions page EDIT"},
                widgets_ids: [1],
                version: 2
              },
              schema: "{{schema.pages}}"
            }
          },
          {
            it: "can get the page with both versions",
            request: "GET /pages/{{page_id}}?relationships=1",
            assert: [
              {
                select: "body.data.relationships.versions.data",
                size: 2
              },
              {
                select: "body.data.attributes",
                equal_keys: {
                  id: "{{page_id}}",
                  version: 2,
                  title: {se: "Versions page EDIT"},
                  slug: page.slug,
                  widgets_ids: [1]
                }
              },
              {
                select: "body.data.relationships.versions.data.0.attributes",
                equal_keys: {
                  id: "{{page_id}}",
                  version: 2,
                  title: {se: "Versions page EDIT"},
                  slug: page.slug,
                  widgets_ids: [1]
                }
              },
              {
                select: "body.data.relationships.versions.data.1.attributes",
                equal_keys: {
                  id: "{{page_id}}",
                  version: 1,
                  title: page.title,
                  slug: page.slug,
                  widgets_ids: page.widgets_ids
                }
              }
            ]
          },
          {
            it: "can get only the first version",
            request: "GET /pages/{{page_id}}?version=1",
            assert: {
              select: "body.data.attributes",
              equal_keys: {
                id: "{{page_id}}",
                version: 1,
                title: page.title,
                slug: page.slug,
                widgets_ids: page.widgets_ids
              }
            }
          },
          {
            it: "can get only the second/last version (default)",
            request: "GET /pages/{{page_id}}",
            assert: {
              select: "body.data.attributes",
              equal_keys: {
                id: "{{page_id}}",
                version: 2,
                title: {se: "Versions page EDIT"},
                slug: page.slug,
                widgets_ids: [1]
              }
            }
          },
        ]
      }
    ]
  }
};
