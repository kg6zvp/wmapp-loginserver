**Get Token**
----
	Authenticates a user to the system and returns a UserToken via json and a base64-encoded RSA signature of the UserToken if authentication is successful

* **URL**

	/loginserver/resources/token/getToken

* **Method:**

	`POST`
	
*	**URL Params**

	`NONE`

* **POST Body**

	Json object with the following attributes:
	* username
	* devicename
	* password
	
	**Required:**
 
		* `username=[string]`
		* `password=[string]`




* **Success Response:**

	* **HTTP Status Code:** 200 OK
	* **Headers in Response:**
		* TokenSignature: WtHSxFAy6yO2Bepb4NgRxYhRUEmKS793gd1NBX/bDErBjD3CTiLA8p05RNIG8U96bkwy
			* the base64 encoded signature of the UserToken being returned
			* The type of signature is SHA-256 with RSA, meaning the json String in the body of the response has a Sha256 digest taken and then that digest is 'signed' using the server's RSA private key. After being decoded from base64 into the raw bytes, anyone with the authentication server's public key can verify a token's authenticity by performing signature verification (google "$yourLanguage verify RSA signature" for information on how to do this)
	* **Response Body:**

	~~~~ {.javascript .numberLines}
	{
		"tokenId": 8728935,
		"studentID": 591082,
		"username": "pkirkland",
		"deviceName": "EisenOS",
		"expirationDate": 1488842937819,
		"blacklisted": false,
		"employeeType": "student",
		"groups": [
			{
				"id": 9235,
				"name": "Software",
				"ldapName": "cn=Software, cn=Groups, dc=example, dc=com"
			},
			{
				"id": 9257,
				"name": "Students",
				"ldapName": "cn=Students, cn=Groups, dc=example, dc=com"
			}
		]
	}
	~~~~


* **Error Response:**

	* **Code:** 401 UNAUTHORIZED
		* Meaning: Incorrect username and/or password provided; we can't/won't specify which one was incorrect as this would represent a huge security problem (search StackExchange if you have questions)
		* **Content:** `{ "error" : "Incorrect Username or Password" }`

		OR

	* **Code:** 500 INTERNAL SERVER ERROR
		* Meaning: A problem occurred and it probably has to do with LDAP
		* **Content:** `{ "error" : "LDAP Error" }`


* **Sample Call:**
	
	**Python:**\
	Requirements:
	* the requests library is installed
	
	~~~~ {.python .numberLines}
	#!/usr/bin/python2.7
	#should be basically the same for python 3.5, just use the package python3-requests
	
	import json #import the json parsing package to retrieve the response as a dictionary
	import requests #import the requests library from python-requests
	
	server = "localhost:8080" #input the FQDN/DNS name of the server, like google.com
	tokenUrl = "http://"+server+"/loginserver/resources/token/getToken" #input the rest of the url
	
	hrs = {'Content-Type': 'application/json'} #include the content-type header to inform the server it should expect json
	
	authenticationJson = '{"username": "pkirkland", "password": "fuzzyBunny1234", "devicename": "EisenOS"}' #This shouldn't ever be a hardcoded string, it is just for clarify in this example; If you want to generate a string of json the right way, look for 'python dictionary to json' on StackExchange or Google
	
	response = requests.post(url=tokenUrl, data=authenticationJson, headers=hrs) #perform the HTTP post request using the url of the API call and the json containing the username and password required to authenticate the user
	
	if response.status_code == 200: #Server returned 200 OK response, that means we've successfully logged in and should have been sent a user token
		print 'Success'
		print 'TokenSignature: '+response.headers['TokenSignature'] #print the token signature in base64 returned by the server
		print 'Token: '+response.content #print the token issued by the server
	
	elif response.status_code == 401: #The server rejected the username or password we sent
		print body['error']
	
	elif response.status_code == 500: #The server had an error accessing LDAP
		print body['error']
	else:
		print 'Not sure what happened here'
		print response.status_code
	~~~~

	The above program produces the following output if the username and password provided are accurate:\
	Success\
	TokenSignature: REAUehHg8ebkw3g3jeYuj/S2zVPUWk/ayPHXNUwm+VSd6C9iWv6cy1xy0dUCPWEQkVwBDWBNYQdzJD6Ky0+pTNGapmcihaCW2kUntVv3vBJPRarFhk1bAQX/+Db8wtkNTQVVIWDJiXDDNaUsyXxvKweL+pf7uBTKvBjO2FW96P+PQU5hkTR3bYz4O8Hcv7z1ts4/LGdCX++QZ6cyIloy7OLs4IXFALIyhVv71y13JGwMtshcT5rgGLbsHj43ZsMkFZgg844cx45lhHBWydEE577WKr0+GOIrYKa0w14UDssEnoDhFW3I1Az56Fvi7zgmd9D0slLyJPiW77eUIC+c4UbF/aqMukmeIMiOVnFiGbOlBcE9UGRtefqkygRIqXFPHxuqMbJE6EzgXvLuZNApFk2b5eCXSdVC/gaAmlBjGeDz6TIBPf9q72xG9SjbdsO3S+0PlTOAmZmleYqCzzTRqnZ/bAaWdcmv/UQtUuCY+uRoBGu6+LRHIOArSthC/Sh45XZ1ZvqWrBSTEf15g1zNmCNghCeGbdI5Z/rUxx21q6Wkodgmca3wwX27G7RdTfoRcdAoRo9bZHrghT2xXmcc8uu0fBbCaWDIZzNeSSQ8GsunO7hL5iAPx4HRo5StjLK4li4Eemi4BDeVuhY/SsvtNcMZFwP6U4Xi12X9BCpdNOk=\
	Token: {"tokenId":4,"studentID":591082,"username":"pkirkland","deviceName":"EisenOS","expirationDate":1492226587235,"blacklisted":false,"employeeType":"student","groups":[{"id":935,"name":"cs","ldapName":"cn=cs,cn=Groups,dc=campus,dc=westmont,dc=edu"}]}
	
	\
	
	**Bash:**\
	Requirements:
	* openssl is installed
	* curl is installed
	* coreutils is installed (should always be present, provides the 'base64' command)
	* public key of the authentication system is in PEM format in the file 'auth-pubkey.pub'
	
	~~~~ {.bash .numberLines}
	#!/bin/bash

	SERVER="wmapp.cs.westmont.edu" #Set the name of the server
	TOKENURL="http://$SERVER/loginserver/resources/token/getToken" #Set the complete URL of the API call
	#For clarity, declare the variables we're about to collect
	USERNAME=""
	PASS=""
	DEVICENAME=""
	
	#Get the username, device name and password from the user
	echo -n "Username:"
	read USERNAME
	echo -n "Device name:"
	read DEVICENAME
	echo -n "Password:"
	read -s PASS #-s flag is used so that the password isn't visible in the terminal

	dataString="{\"username\" : \"$USERNAME\", \"password\" : \"$PASS\", \"devicename\" : \"$DEVICENAME\"}" #Create the json string with the username, password and device name inside
	curl -XPOST -H 'Content-Type: application/json' -d "$dataString" --dump-header "resp-headers.txt" "$TOKENURL" -o "token.json" #Perform an HTTP post request against the API endpoint, saving the headers returned from the server in resp-headers.txt and saving the body of the response from the server in token.json

	#extract signature and decode base64 into binary
	cat resp-headers.txt |grep TokenSignature|cut -d: -f2|sed 's/^ //'> sigb64.txt #extract the token's signature into the file sigb64.txt
	base64 -d sigb64.txt> sig.bin #decode the base64 encoded signature back into it's natural binary form and write it into the file sig.bin
	
	#Verify the signature with OpenSSL using the SHA256 with RSA signature algorithm
	openssl dgst -sha256 -verify auth-pubkey.pub -signature sig.bin token.json #This command will print Verification failed or Verification ok
	~~~~


