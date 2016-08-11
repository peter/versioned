"use strict";

var page = {
  "title": {
    "se": "Testsida"
  },
  "slug": {
    "se": "testsida"
  }
};

var resourceSchema = {
  type: "object",
  properties: {
    id: {type: "string"},
    type: {enum: ["pages"]},
    attributes: {type: "object"}
  },
  additionalProperties: false,
  required: ["id", "type", "attributes"]
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
            params: {data: {attributes: page}},
            assert: [
              {
                select: "body.data.attributes",
                equal_keys: {
                  title: page.title
                },
                schema: "{{schema.pages}}"
              },
              {
                select: "body.data",
                schema: resourceSchema
              }
            ],
            save: {
              "page_id": "body.data.attributes.id"
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
            it: "can get the changelog entry for the create",
            request: "GET /changelog",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  action: "create",
                  created_by: "{{users.admin.email}}"
                }
              },
              {
                select: "body.data.0.attributes.doc",
                equal_keys: {
                  id: "{{page_id}}",
                  title: page.title
                }
              }
            ]
          },
          {
            it: "can list page documents",
            request: "GET /pages",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  id: "{{page_id}}",
                  title: page.title
                },
                schema: "{{schema.pages}}"
              },
              {
                select: "body.data",
                schema: {
                  type: "array",
                  items: resourceSchema
                }
              }
            ]
          },
          {
            it: "can update name of page document",
            request: "PUT /pages/{{page_id}}",
            params: {
              data: {attributes: {title: {se: "Testsida EDIT"}}}
            },
            assert: {
              select: "body.data.attributes",
              equal_keys: {
                id: "{{page_id}}",
                title: {se: "Testsida EDIT"}
              },
              schema: "{{schema.pages}}"
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
              },
              schema: "{{schema.pages}}"
            }
          },
          {
            it: "can get the changelog entry for the update",
            request: "GET /changelog",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  action: "update",
                  created_by: "{{users.admin.email}}"
                }
              },
              {
                select: "body.data.0.attributes.doc",
                equal_keys: {
                  id: "{{page_id}}",
                  title: {se: "Testsida EDIT"}
                }
              },
              {
                select: "body.data.0.attributes.changes.title",
                equal_keys: {
                  from: page.title,
                  to: {se: "Testsida EDIT"}
                }
              }
            ]
          },
          {
            it: "can delete page document",
            request: "DELETE /pages/{{page_id}}"
          },
          {
            it: "can verify that page document was deleted",
            request: "GET /pages/{{page_id}}",
            status: 404
          },
          {
            it: "can get the changelog entry for the delete",
            request: "GET /changelog",
            assert: [
              {
                select: "body.data.0.attributes",
                equal_keys: {
                  action: "delete",
                  created_by: "{{users.admin.email}}"
                }
              },
              {
                select: "body.data.0.attributes.doc",
                equal_keys: {
                  id: "{{page_id}}",
                  title: {se: "Testsida EDIT"}
                }
              }
            ]
          }
        ]
      }
    ]
  }
};
