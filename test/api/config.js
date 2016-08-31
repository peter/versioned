"use strict";

module.exports = {
  config: {
    log_path: "test/api/log",
    modules: {
      http_client: './http_clients/request'
    },
    defaults: {
      api_call: {
        request: {
          base_url: "http://localhost:5001/v1",
          headers: "{{headers.admin}}"
        },
        status: 200
      }
    }
  }
};
