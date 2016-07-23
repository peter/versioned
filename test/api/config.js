"use strict";

module.exports = {
  config: {
    defaults: {
      api_call: {
        request: {
          base_url: "http://localhost:5000/v1",
          headers: "{{headers.admin}}"
        },
        status: 200
      }
    }
  }
};
