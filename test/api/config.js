"use strict";

module.exports = {
  config: {
    defaults: {
      api_call: {
        request: {
          base_url: "http://localhost:5000/v1"
        },
        status: 200
      }
    }
  },
  data: {
    headers: {
      admin: {
        Authorization: "Bearer: {{$env.TOKEN}}"
      },
      invalid: {
        Authorization: "Bearer: this-is-an-invalid-token"
      }
    }
  }
};
