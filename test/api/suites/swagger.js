"use strict";

var swaggerSchema = require('../../../resources/swagger-2.0-schema');

module.exports = {
  suite: {
    name: "swagger",
    tests: [
      {
        name: "schema-valid",
        description: "The swagger.json endpoint delivers a schema that conforms with Swagger 2.0",
        api_calls: [
          {
            it: "has a valid schema",
            request: "GET /swagger.json",
            assert: {
              schema: swaggerSchema
            }
          },
        ]
      }
    ]
  }
};
