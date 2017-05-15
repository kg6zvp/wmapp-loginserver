#!/bin/bash

SERVER="auth.wmapp.mccollum.enterprises"
USERNAME=""
DEVICENAME=""
exec 2> /dev/null #may need to remove error redirection

# Check if file exists and delete
function rem(){
	if [[ -f "$1" ]]; then
		rm "$1"
	fi
}

# Remove temp files
function removeTempFiles(){
	rem resp-headers.txt
	rem token.json
	rem sigb64.txt
	rem sig.bin
}

# Extract signature and write it into a binary file
function extractSignature(){
	cat resp-headers.txt |grep TokenSignature|cut -d: -f2|sed 's/^ //'> sigb64.txt
	base64 -d sigb64.txt> sig.bin
}

function doVerification(){
	openssl dgst -sha256 -verify cryptoStuff/pubkey.pub -signature sig.bin token.json
}

function doLogin(){
	# Get password from user
	echo -n "Password:"
	read -s PASS
	echo

	#do curl stuff
	dataString="{\"username\" : \"$1\", \"password\" : \"$PASS\", \"devicename\" : \"$2\"}"
	curl -XPOST --insecure -H 'Content-Type: application/json' -d "$dataString" --dump-header "resp-headers.txt" "https://$SERVER/api/token/getToken" -o "token.json"
}

function printHelp(){
	echo "Used to test the login server with the public key located in cryptoStuff/pubkey.pub"
	echo
	echo "Usage: ./test.sh [username] [deviceName]"
	echo "If no devicename is provided, the computer's hostname will be used"|sed 's/^/\t/'
	echo "If no arguments are provided, the username on your computer will be used as the username and the computer's hostname will be used as the device name"|sed 's/^/\t/'
}

#prepare for script
case $# in
	0)
		USERNAME="$(whoami)"
		DEVICENAME="$(hostname -s)"
		;; # no arguments provided
	1)
		if [[ "$1" == "clean" ]]; then
			echo "Cleaning temporary files"
			removeTempFiles
			exit
		elif [[ "$1" == "help" ]]; then
			printHelp
			exit
		fi
		USERNAME="$1"
		DEVICENAME="$(hostname -s)"
		;; # just the username provided
	2)
		USERNAME="$1"
		DEVICENAME="$2"
		;; # username and device name provided
	*) echo "What are you doing?";; # They provided a bunch of arguments
esac

removeTempFiles
doLogin "$USERNAME" "$DEVICENAME"
extractSignature
doVerification
exit
