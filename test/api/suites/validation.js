"use strict";

var page = {
  "title": {
    "se": "Validations page"
  },
  "slug": {
    "se": "validation-page"
  },
  "widgets_ids": [1, 2]
};

module.exports = {
  suite: {
    name: "validation",
    tests: [
      {
        name: "validation of a page",
        api_calls: [
          {
            it: "cannot create with missing data attribute",
            request: "POST /pages",
            params: {pages: {attributes: {slug: {se: "validations-page"}}}},
            status: 422
          },
          {
            it: "cannot update page that doesn't exist",
            request: "PUT /pages/0",
            params: {pages: {attributes: {slug: {se: "validations-page"}}}},
            status: 404
          },
          {
            it: "cannot update page with missing data attribute",
            request: "PUT /pages/{{pages.start.id}}",
            params: {pages: {attributes: {slug: {se: "validations-page"}}}},
            status: 422
          },
          {
            it: "cannot create without a title (required)",
            request: "POST /pages",
            params: {data: {attributes: {slug: {se: "validations-page"}}}},
            status: 422
          },
          {
            it: "cannot create a page document with a title with an invalid locale",
            request: "POST /pages",
            params: {data: {attributes: {title: {see: "Validations page"}, slug: {se: "validations-page"}}}},
            status: 422
          },
          {
            it: "cannot create a page document with widgets_ids as a string (invalid type)",
            request: "POST /pages",
            params: {data: {attributes: {title: {se: "Validations page"}, slug: {se: "validations-page"}, widgets_ids: "1, 2"}}},
            status: 422
          },
          {
            it: "cannot create a page with unrecognized properties",
            request: "POST /pages",
            params: {data: {attributes: {title: {se: "Validations page"}, slug: {se: "validations-page"}, foo: "bar"}}},
            status: 422
          },
        ]
      },

      {
        name: "database errors",
        api_calls: [
          {
            it: "violating a unique db index yields a validation error",
            request: "POST /pages",
            params: {data: {attributes: {title: {se: "Startsida"}}}},
            status: 422,
            assert: {
              select: "body.errors.0",
              equal_keys: {
                type: "duplicate_key",
                message: /E11000 duplicate key error index/
              }
            }
          },
        ]
      }
    ]
  }
};
