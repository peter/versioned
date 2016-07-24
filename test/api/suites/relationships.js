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
    name: "relationships",
    tests: [
      {
        name: "pages-widgets to-many relationship",
        description: "You can fetch a page with its widgets relationships",
        api_calls: [
          {
            it: "can create a page document without widgets",
            request: "POST /pages",
            params: {data: {attributes: page}},
            save: {
              "page_id": "body.data.attributes.id"
            }
          },
          {
            it: "can get the created page document with empty widgets relationship",
            request: "GET /pages/{{page_id}}?relationships=1",
            assert: [
              {
                select: "body.data.attributes",
                equal_keys: {
                  id: "{{page_id}}",
                  title: page.title
                }
              },
              {
                select: "body.data.relationships.widgets.data",
                equal: []
              }
            ]
          },
          {
            it: "can add two widgets and a non-existant widget to the page",
            request: "PUT /pages/{{page_id}}",
            params: {data: {attributes: {
              widgets_ids: [1, 2, 9999999]
            }}},
            save: {
              "page_id": "body.data.attributes.id"
            }
          },
          {
            it: "can get the created page document with two widgets relationships",
            request: "GET /pages/{{page_id}}?relationships=1",
            assert: [
              {
                select: "body.data.relationships.widgets.data",
                size: 2,
                schema: {
                  type: "array",
                  items: {
                    type: "object",
                    properties: {
                      id: {type: "string"},
                      type: {type: "string"},
                      attributes: "{{schema.widgets}}"
                    },
                    additionalProperties: false,
                    required: ["id", "type", "attributes"]
                  }
                }
              },
              {
                select: "body.data.relationships.widgets.data.attributes.id",
                equal: [1, 2]
              }
            ]
          }
        ]
      }
    ]
  }
};
