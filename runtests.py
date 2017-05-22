#!/usr/bin/env python2
#/usr/bin/python2

import json
import requests
import sys

protocol = "http"
server = "auth.wmapp.mccollum.enterprises"

authBaseUrl = protocol+"://"+server+"/api"
tokenBaseUrl = authBaseUrl+"/token"
usersBaseUrl = authBaseUrl+"/users"

loginUrl = tokenBaseUrl+"/getToken"
logoutUrl = tokenBaseUrl+"/invalidateToken"
listUrl = tokenBaseUrl+"/listTokens"
renewUrl = tokenBaseUrl+"/renewToken"
invalidationSubscriptionUrl = tokenBaseUrl+"/subscribeToInvalidation"
tokenValidUrl = tokenBaseUrl+"/tokenValid"
userInfoUrl=authBaseUrl+"/user"

tokenSignature=" "
tokenString=" "

def readTokens():
    with open('token.json', 'rb') as tf:
	tokenString = tf.read()
    with open('sigb64.txt', 'rb') as sf:
	tokenSignature = sf.read().strip('\n').strip('\r')

def getToken(username, password, deviceName):
    hrs = {'Content-Type': 'application/json'}
    loginObject = {'username': username, 'password': password, 'devicename': deviceName}
    return requests.post(url=loginUrl, data=json.dumps(loginObject), headers=hrs)

def invalidateToken(delToken, token, sigb64):
    hrs = {'Content-Type': 'application/json'}
    hrs['Token'] = token
    hrs['TokenSignature'] = sigb64
    return requests.delete(url=logoutUrl+'/'+str(delToken['tokenId']), headers=hrs)

def listTokens(token, sigb64):
    hrs = {'Content-Type': 'application/json'}
    hrs['Token'] = token
    hrs['TokenSignature'] = sigb64
    return requests.get(url=listUrl, headers=hrs)

def renewToken(token, sigb64):
    hrs = {'Content-Type': 'application/json'}
    hrs['Token'] = token
    hrs['TokenSignature'] = sigb64
    return requests.get(url=renewUrl, headers=hrs)

def subscribeToInvalidation(invalidationSubscription, token, sigb64):
    hrs = {'Content-Type': 'application/json'}
    hrs['Token'] = token
    hrs['TokenSignature'] = sigb64
    return requests.post(url=invalidationSubscriptionUrl, headers=hrs)

def isValidToken(token, sigb64):
    hrs = {'Content-Type': 'application/json'}
    hrs['Token'] = token
    hrs['TokenSignature'] = sigb64
    return requests.get(url=tokenValidUrl, headers=hrs)

def getUserInfo(token, sigb64):
    hrs={'Content-Type': 'application/json'}
    hrs['Token'] = token
    hrs['TokenSignature'] = sigb64
    return requests.get(url=userInfoUrl, headers=hrs)


def checkCode(httpResponse, expectedResponse, failureMessage):
    if httpResponse.status_code != expectedResponse:
	print "\tFailed to "+failureMessage
	print "\t"+str(httpResponse.status_code)
	print "\t"+httpResponse.content
	sys.exit(1)

#VALID TESTS
print "Testing valid login..."
validCreds = getToken('erichtofen', 'oneStupidLongTestPassword23571113', 'validDevice')
checkCode(validCreds, 200, "login")
print "\tLogin successful!"

print "Testing valid login with @westmont.edu suffix..."
validCreds = getToken('erichtofen@westmont.edu', 'oneStupidLongTestPassword23571113', 'validEmail1Device')
checkCode(validCreds, 200, "login")
print "\tLogin successful!"

print "Testing valid login with @westmont suffix..."
validCreds = getToken('erichtofen@westmont', 'oneStupidLongTestPassword23571113', 'validEmail2Device')
checkCode(validCreds, 200, "login")
print "\tLogin successful!"

print "Testing validation check on valid validCreds..."
validCheckResponse = isValidToken(validCreds.content, validCreds.headers['TokenSignature'])
checkCode(validCheckResponse, 200, "validate credentials")
print "\tValidation successful"

print "Testing list tokens..."
validList = listTokens(validCreds.content, validCreds.headers['TokenSignature'])
checkCode(validList, 200, "list tokens")
print "\tListing tokens successful"

print "Testing token renewal..."
validRenewal = renewToken(validCreds.content, validCreds.headers['TokenSignature'])
checkCode(validRenewal, 200, "renew token")
print "\tRenewing token successful"

print "Testing get user info..."
userinfo = getUserInfo(validRenewal.content, validRenewal.headers['TokenSignature'])
checkCode(userinfo, 200, "get user info")
print userinfo.content
print "\tRetrieving user info successful"

print "Testing token invalidation..."
validInvalidation = invalidateToken(json.loads(validRenewal.content), validRenewal.content, validRenewal.headers['TokenSignature'])
checkCode(validInvalidation, 200, "invalidate token")
print "\tToken invalidation successful"




#INVALID TESTS
print "Testing invalid login..."
invalidCreds = getToken('student12234', 'notMyPassword1234', 'invalidDevice')
checkCode(invalidCreds, 401, "proper rejection of invalid credentials")
print "\tLogin with invalid credentials failed correctly!"


