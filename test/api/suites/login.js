"use strict";

var assert = require('assert');

module.exports = {
  suite: {
    name: "login",
    tests: [
      {
        name: "login-and-get-token",
        description: "You can get an access token with valid credentials",
        api_calls: [
          {
            it: "cannot log in with invalid email",
            request: "POST /login",
            params: {email: "not-valid-email", password: "{{users.admin.password}}"},
            status: 401
          },
          {
            it: "cannot log in with invalid password",
            request: "POST /login",
            params: {email: "{{users.admin.email}}", password: "not-valid-password"},
            status: 401
          },
          {
            it: "can log in with valid email and password and get auth token",
            request: "POST /login",
            params: {email: "{{users.admin.email}}", password: "{{users.admin.password}}"},
            assert: [{
                select: "body.data.attributes",
                equal_keys: {
                  email: "{{users.admin.email}}"
                }
              },
              {
                select: "body.data.attributes.access_token",
                equal: /[a-z0-9]{10,}/
              },
              {
                select: "body.data.attributes.password",
                equal: null
              },
              function(body, headers) {
                assert.equal(headers.authorization, ("Bearer " + body.data.attributes.access_token));
              }],
            save: {
              "headers.admin.Authorization": "headers.authorization"
            }
          },
        ]
      }
    ]
  }
};
