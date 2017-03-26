#!/bin/bash

function clean(){
	rm -rf apidocs
}

function make(){
	clean
	apidoc -i ./ -o apidocs/
}

if [ $# -eq 0 ]; then
	echo "clean and make"
	make
else
	if [[ "$1" == "help" ]]; then
		echo "Run without arguments to clean, then make docs"
		echo "Run with the argument either 'clean' or 'make' in order to specify the operation to be performed"
		exit
	fi
	echo "running script"
	$1
fi
