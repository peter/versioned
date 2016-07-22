"use strict";

var assert = require('assert');

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
            params: {pages: "{{pages.start}}"},
            headers: "{{headers.admin}}",
            // status: 201,
            // assert: function(body) {
            //   assert(body.createdAt);
            //   assert(new Date(body.createdAt) > new Date());
            //   this.pages.start.id = body.id; // save the ID
            // }
          },
          // {
          //   it: "can get the created page document",
          //   request: "GET /pages/{{pages.start.id}}",
          //   assert: function(body) {
          //     assert.equal(body.playerName, this.pages.start.playerName);
          //   }
          // },
          // {
          //   it: "can list page documents",
          //   request: "GET /pages",
          //   params: {
          //     order: "-created_at",
          //     limit: 3
          //   },
          //   assert: function(body) {
          //     assert.equal(body.results.length, 3);
          //     assert.equal(body.results[0].id, this.pages.start.id);
          //   }
          // },
          // {
          //   it: "can update name of page document",
          //   request: "PUT /pages/{{pages.start.id}}",
          //   params: {
          //     playerName: "New name"
          //   }
          // },
          // {
          //   it: "can get new name of page document",
          //   request: "GET /pages/{{pages.start.id}}",
          //   assert: function(body) {
          //     assert.equal(body.playerName, "New name");
          //   }
          // },
          // {
          //   it: "can delete page document",
          //   request: "DELETE /pages/{{pages.start.id}}"
          // },
          // {
          //   it: "can verify that page document was deleted",
          //   request: "GET /pages/{{pages.start.id}}",
          //   status: 404
          // }
        ]
      }
    ]
  }
};
