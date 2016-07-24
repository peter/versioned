"use strict";

module.exports = {
  config: {
    log_path: "test/api/log",
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
