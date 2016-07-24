"use strict";

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
            save: {
              "headers.admin.Authorization": "headers.authorization"
            }
          },
        ]
      }
    ]
  }
};
