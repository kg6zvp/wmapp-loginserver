define({ "api": [
  {
    "type": "get",
    "url": "api/key/getPubKey",
    "title": "Request Public Key",
    "name": "GetPubKey",
    "group": "Key",
    "description": "<p>This method is for requesting the public key which is used to verify the signature on a signed user token. The public key is a 4096-bit RSA key.</p>",
    "examples": [
      {
        "title": "Usage",
        "content": "The key is used by servers employing this login system in the following way:\n(Note: The key is a 4096-bit RSA key)\n1.) Take a SHA-256 hash of the token presented by the client\n2.) Verify that the signature presented by the client (which is a signed version of the SHA-256 hash of the token encoded in UTF-8 and containing no leading or trailing spaces or zero-length characters) was signed by the private key using the public key given by this API endpoint",
        "type": "String"
      },
      {
        "title": "Success Response",
        "content": "-----BEGIN PUBLIC KEY-----\nMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvTdcdxIfo7iO0viB5TUl \nuEBPw4BaAtKPgOonxPfuSfOHP1SkBau/2NhJLKSc/P8gH4TrAnbuW3a14VeyQtBo\nnZxYaRetD8wS2JRRfswYJve8jtWUcE7I0a5JjBqCCgdNFXQcubh33ilj+WPvUX7X\nMXkNyQZ+4IOKP8iTo3OD5TzLJT17A5n5flvPQfXbLrzzBCqGxTgG3kLpx6Ya6YO7\nkxPDZbRFgstV2sobBIhsA5bdw92r/UA0SZlojinnsxcj2XRwutBD1iZG/nyK8Rd3\nPfIys9KhqQs1nddz5Zy3ZcaPNnEQaYZzVO5nKPBGuEprTOLHS4soclWz/Rbd6PDr\n43Sax0i3mYiNXxOsFUjBo8RfVknkahwqDeXxxNTm86cu/kUXoOm2nTTFzHgnO7Qm\nQMWzadT9BD3F34HDUYACXJnWDHRCnyJpQ48G9vR9clPSd37ygq0MPpkyI+yJ4mlL\nxNjRtsm1hKCm4rHtoBl7D3GNP1REm+CUvKcMJaCgrQ+W2Cz0PYSGipaiRNw3YrCz\nQZHAiEkyyAvvox647cRd/RPjHAtaPwuuB+ifAuCrmTqyNWLMoRBuJwwPiNiMEX+y\ntNdHLNt/WKGTl55mDC7fqp9unq8PNdkOCKMLw3uJyIDg3C7J/kY11XL6URCRtGAa\nUpRIx64HdyFNybeS3mMi5bkCAwEAAQ==\n-----END PUBLIC KEY-----",
        "type": "String"
      }
    ],
    "version": "0.0.0",
    "filename": "./src/main/java/enterprises/mccollum/wmapp/loginserver/jax/KeyResources.java",
    "groupTitle": "Key"
  },
  {
    "type": "get",
    "url": "api/token/listTokens",
    "title": "Get Token List",
    "name": "GetTokenList",
    "group": "Token",
    "description": "<p>This call is for retrieving all the tokens associated with the user account.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "Token",
            "description": "<p>The User Token used to retrieve all tokens associated with the user.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "TokenSignature",
            "description": "<p>The base64 encoded SHA256 RSA signature of the token that needs to be verified.</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Response Error": [
          {
            "group": "Response Error",
            "type": "500",
            "optional": false,
            "field": "INTERNAL_SERVER_ERROR",
            "description": "<p>Can be No Such Algorithm, Invalid Signature, or Invalid Public Key.</p>"
          },
          {
            "group": "Response Error",
            "type": "401",
            "optional": false,
            "field": "UNAUTHORIZED",
            "description": "<p>The username or password was incorrect.</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Success Response",
        "content": "[\n  {\n    \"tokenId\": 4,\n    \"studentID\": 935,\n    \"username\": \"erichtofen\",\n    \"deviceName\": \"box\",\n    \"employeeType\": \"student\",\n    \"expirationDate\": 1492934768266,\n    \"blacklisted\": false\n  },\n  {\n    \"tokenId\": 5,\n    \"studentID\": 935,\n    \"username\": \"erichtofen\",\n    \"deviceName\": \"mk2\",\n    \"employeeType\": \"student\",\n    \"expirationDate\": 1492934837502,\n    \"blacklisted\": false\n  }\n]",
        "type": "json"
      }
    ],
    "version": "0.0.0",
    "filename": "./src/main/java/enterprises/mccollum/wmapp/loginserver/jax/TokenResources.java",
    "groupTitle": "Token"
  },
  {
    "type": "get",
    "url": "api/token/tokenValid",
    "title": "Check Valid Token",
    "name": "GetTokenValid",
    "group": "Token",
    "description": "<p>This call is for checking if a User Token is valid.</p>",
    "header": {
      "fields": {
        "Header": [
          {
            "group": "Header",
            "type": "String",
            "optional": false,
            "field": "TokenSignature",
            "description": "<p>The base64 encoded signature of the token</p>"
          },
          {
            "group": "Header",
            "type": "json",
            "optional": false,
            "field": "Token",
            "description": "<p>The token that needs to be checked.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./src/main/java/enterprises/mccollum/wmapp/loginserver/jax/TokenResources.java",
    "groupTitle": "Token"
  },
  {
    "type": "delete",
    "url": "api/token/invalidateToken/{tokenId}",
    "title": "Invalidate Token",
    "name": "InvalidateToken",
    "group": "Token",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "tokenID",
            "description": "<p>{long} The ID of the token to be deleted</p>"
          }
        ]
      }
    },
    "description": "<p>This call allows a user to invalidate a token on another device that they are signed in on.</p>",
    "header": {
      "fields": {
        "Header": [
          {
            "group": "Header",
            "optional": false,
            "field": "Token",
            "description": "<p>The User Token used to authenticate this request</p>"
          },
          {
            "group": "Header",
            "optional": false,
            "field": "TokenSignature",
            "description": "<p>The base64 encoded signature of the token used to authenticate</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./src/main/java/enterprises/mccollum/wmapp/loginserver/jax/TokenResources.java",
    "groupTitle": "Token"
  },
  {
    "type": "post",
    "url": "api/token/getToken",
    "title": "Get Token",
    "name": "PostGetToken",
    "group": "Token",
    "description": "<p>This call will retrieve a User Token to use for all microservices</p>",
    "parameter": {
      "fields": {
        "credentials": [
          {
            "group": "credentials",
            "type": "String",
            "optional": false,
            "field": "username",
            "description": "<p>Username of the user to login</p>"
          },
          {
            "group": "credentials",
            "type": "String",
            "optional": true,
            "field": "devicename",
            "description": "<p>The name of the device being logged in from. This is used to help the user identify which devices they're logged in on. If unspecified, a random UUID will be generated for this value</p>"
          },
          {
            "group": "credentials",
            "type": "String",
            "optional": false,
            "field": "password",
            "description": "<p>The password of the user account to be logged into</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Response Error": [
          {
            "group": "Response Error",
            "type": "401",
            "optional": false,
            "field": "UNAUTHORIZED",
            "description": "<p>The username or password was incorrect.</p>"
          },
          {
            "group": "Response Error",
            "type": "500",
            "optional": false,
            "field": "INTERNAL_SERVER_ERROR",
            "description": "<p>There was an error with the LDAP query.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success Response Header": [
          {
            "group": "Success Response Header",
            "type": "String",
            "optional": false,
            "field": "TokenSignature",
            "description": "<p>The base64 encoded signature of the user's token (<a href=\"http://lmgtfy.com?iie=1&q=what+is+base64+encoding\" target=\"_blank\">What is base64 encoding</a>)</p>"
          }
        ],
        "Success 200": [
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "tokenId",
            "description": "<p>The UUID of the token generated</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "studentId",
            "description": "<p>The student ID of the user.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "username",
            "description": "<p>The username of the user.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "devicename",
            "description": "<p>The name of the device being used.</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "expirationDate",
            "description": "<p>The expiration date/time of the token in milliseconds using EPOCH time (<a href=\"http://lmgtfy.com/?iie=1&q=what+is+epoch+time\" target=\"_blank\">What is epoch time</a>)</p>"
          },
          {
            "group": "Success 200",
            "type": "boolean",
            "optional": false,
            "field": "blacklisted",
            "description": "<p>The token's revocation status (will be true if token has been invalidated or revoked)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "employeeType",
            "description": "<p>The type of the user (most commonly: student, facstaff, alum, or community)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Success Response Header Example",
        "content": "TokenSignature: NU2edZPpt1RjkvJjNM2t1l/fP0p8in+6mqk7Nh6Govxo6EZaei4B16iHMLDY0PwB/FvAvZwQEuT25l6CQSLTC4sC8KBWdIDGTV/k698ZEOqoytibRU05AKrGmcSZsdfqdhZAS9cp1apGTQXrijP/0BicpjIM+sVB71sN/mMecsVSG1qHJxpiothNgcuJCG0uBgMwLKpuhhZ67s6kDbr7pyH49bal4ooBfbmS50PcaN5IhFaD7YtOb1FRD6dK0DgYwcjOulfQ4I3HXgnQ1i9IWXjQbFKSFNlpg414yW9tA7xgcL3bvIiRSpruW6J2LaOKQNv9qQO5wXbcQ3BrWXPc7jbljrH8296kBfhzPmAtH2xDg4uzI/JRby7NS5ftDGOouP6ptBp/Do4pMQviPDX46dcYzD5c=",
        "type": "json"
      },
      {
        "title": "Success Response Body Example",
        "content": "{\n  \"tokenId\": 5,\n  \"studentID\": 851,\n  \"username\": \"spidey\",\n  \"deviceName\": \"iphone7\",\n  \"employeeType\": \"student\",\n  \"expirationDate\": 1492934837502,\n  \"blacklisted\": false\n}",
        "type": "json"
      }
    ],
    "version": "0.0.0",
    "filename": "./src/main/java/enterprises/mccollum/wmapp/loginserver/jax/TokenResources.java",
    "groupTitle": "Token"
  },
  {
    "type": "post",
    "url": "api/token/subscribeToInvalidation",
    "title": "Subscribe To Token Invalidation",
    "name": "PostInvalidTokenSubscription",
    "group": "Token",
    "description": "<p>This call allows a microservice to subscribe to updates from the central server for token invalidations.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "Token",
            "description": "<p>The User Token used to authenticate.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "TokenSignature",
            "description": "<p>The base64 encoded SHA256 RSA signature of the token that needs to be verified.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "./src/main/java/enterprises/mccollum/wmapp/loginserver/jax/TokenResources.java",
    "groupTitle": "Token"
  },
  {
    "type": "get",
    "url": "api/token/renewToken",
    "title": "Renew Token",
    "name": "PostRenewToken",
    "group": "Token",
    "description": "<p>This call allows a user to update a User Token with a new expiration date.</p>",
    "header": {
      "fields": {
        "Header": [
          {
            "group": "Header",
            "type": "String",
            "optional": false,
            "field": "TokenSignature",
            "description": "<p>The base64 encoded SHA256 RSA signature of the token that needs to be verified.</p>"
          },
          {
            "group": "Header",
            "type": "json",
            "optional": false,
            "field": "Token",
            "description": "<p>The User Token used to authenticate.</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Response Error": [
          {
            "group": "Response Error",
            "type": "500",
            "optional": false,
            "field": "INTERNAL_SERVER_ERROR",
            "description": "<p>Can be No Such Algorithm, Invalid Signature, or Invalid Public Key.</p>"
          },
          {
            "group": "Response Error",
            "type": "401",
            "optional": false,
            "field": "UNAUTHORIZED",
            "description": "<p>The username or password was incorrect.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "tokenId",
            "description": "<p>The UUID of the token generated</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "studentId",
            "description": "<p>The student ID of the user.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "username",
            "description": "<p>The username of the user.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "devicename",
            "description": "<p>The name of the device being used.</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "expirationDate",
            "description": "<p>The expiration date of the token in milliseconds EPOCH time.</p>"
          },
          {
            "group": "Success 200",
            "type": "boolean",
            "optional": false,
            "field": "blacklisted",
            "description": "<p>The status of the token. Will be true if invalidateToken has been called on this token.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "employeeType",
            "description": "<p>The type of the user. Can be student, facstaff, alum, or community.</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Success Response Header Example",
        "content": "TokenSignature: NU2edZPpt1RjkvJjNM2t1l/fP0p8in+6mqk7Nh6Govxo6EZaei4B16iHMLDY0PwB/FvAvZwQEuT25l6CQSLTC4sC8KBWdIDGTV/k698ZEOqoytibRU05AKrGmcSZsdfqdhZAS9cp1apGTQXrijP/0BicpjIM+sVB71sN/mMecsVSG1qHJxpiothNgcuJCG0uBgMwLKpuhhZ67s6kDbr7pyH49bal4ooBfbmS50PcaN5IhFaD7YtOb1FRD6dK0DgYwcjOulfQ4I3HXgnQ1i9IWXjQbFKSFNlpg414yW9tA7xgcL3bvIiRSpruW6J2LaOKQNv9qQO5wXbcQ3BrWXPc7jbljrH8296kBfhzPmAtH2xDg4uzI/JRby7NS5ftDGOouP6ptBp/Do4pMQviPDX46dcYzD5c=",
        "type": "json"
      },
      {
        "title": "Success Response Body Example",
        "content": "{\n  \"tokenId\": 5,\n  \"studentID\": 851,\n  \"username\": \"spidey\",\n  \"deviceName\": \"iphone7\",\n  \"employeeType\": \"student\",\n  \"expirationDate\": 1492934837502, // Will return with updated Expiration Date\n  \"blacklisted\": false\n}",
        "type": "json"
      }
    ],
    "version": "0.0.0",
    "filename": "./src/main/java/enterprises/mccollum/wmapp/loginserver/jax/TokenResources.java",
    "groupTitle": "Token"
  },
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./src/main/webapp/docs/main.js",
    "group": "_home_smccollum_workspace_loginserver_src_main_webapp_docs_main_js",
    "groupTitle": "_home_smccollum_workspace_loginserver_src_main_webapp_docs_main_js",
    "name": ""
  },
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./target/loginserver/docs/main.js",
    "group": "_home_smccollum_workspace_loginserver_target_loginserver_docs_main_js",
    "groupTitle": "_home_smccollum_workspace_loginserver_target_loginserver_docs_main_js",
    "name": ""
  }
] });
