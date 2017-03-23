#!/usr/bin/env python2
#/usr/bin/python2

import json
import requests

server = "auth.wmapp.mccollum.enterprises"

renewUrl = "http://"+server+"/resources/token/renewToken"
listUrl = "http://"+server+"/resources/token/listTokens"

tokenSignature=" "
tokenString=" "

with open('token.json', 'rb') as tf:
    tokenString = tf.read()

with open('sigb64.txt', 'rb') as sf:
    tokenSignature = sf.read().strip('\n').strip('\r')

hrs = {'Content-Type': 'application/json', 'TokenSignature': tokenSignature}

response = requests.post(url=renewUrl, data=tokenString, headers=hrs)

print response.status_code
print response.headers['TokenSignature']
print response.content

print "Renewing again"

tokenString = response.content
hrs['TokenSignature'] = response.headers['TokenSignature']
response = requests.post(url=renewUrl, data=tokenString, headers=hrs)

print response.status_code
print response.headers['TokenSignature']
print response.content

print "listing outstanding tokens"

tokenString = response.content
hrs['Token'] = tokenString
hrs['TokenSignature'] = response.headers['TokenSignature']
response = requests.get(url=listUrl, headers=hrs)

print response.content
