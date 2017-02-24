#!/bin/bash

echo -n "Password:"
read -s PASS
echo

dataString="{\"username\" : \"$1\", \"password\" : \"$PASS\", \"devicename\" : \"$2\"}"

curl -i -XPOST -H 'Content-Type: application/json' -d "$dataString" http://localhost:8080/loginserver/resources/token/getToken

echo
