#!/bin/bash

openssl genrsa -out privkey.pem 2048 #generate private key

openssl rsa -in privkey.pem -pubout> pubkey.pub #dump public key

openssl req -x509 -sha256 -new -key privkey.pem -out carequest.csr #create certificate signing request
openssl x509 -sha256 -days 7304 -in carequest.csr -signkey privkey.pem -out selfsigned.crt #generate 20-year CA

#Create PKCS12 keystore
openssl pkcs12 -export -name WMAUTH -in selfsigned.crt -inkey privkey.pem -out pkcskeystore.p12
#Convert PKCS12 keystore to Java KeyStore
keytool -importkeystore -destkeystore wmks.jks -srckeystore pkcskeystore.p12 -srcstoretype pkcs12 -alias WMAUTH
keytool -list -v -keystore wmks.jks

