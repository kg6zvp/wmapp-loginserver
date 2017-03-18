#!/usr/bin/python2.7
#should be basically the same for python 3.5, just use the package python3-requests

import json #import the json parsing package to retrieve the response as a dictionary
import requests #import the requests library from python-requests

server = "wmapp.cs.westmont.edu" #input the FQDN/DNS name of the server, like google.com
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
