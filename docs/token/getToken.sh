#!/bin/bash

SERVER="localhost:8080" #Set the name of the server
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
