APIs:

In|Out
Credentials, Device Name|Token
Token|New Token
Token|Extended User Info
Token|Valid?
Token|List of Valid Tokens
Token|Invalidate Token

Token, URL|Subscribe to Invalidation Notices
Token|Raw LDAP Info (low priority)

/user
	/getUserInfo
	/getUserInfoLdap //low priority
/token
	/getToken
	/renewToken
	/tokenValid
	/invalidateToken
	/listTokens
	/subscribeToInvalidation //low priority
