

MAVEN = mvn

default: server

localsvr: pom.xml
	echo "//TODO: Remove datastore specification"
	echo "//TODO: Checkout original version from git"

server: pom.xml
	apidoc -i ./ -o src/main/webapp/
	$(MAVEN) package

clean: pom.xml
	mv src/main/webapp/WEB-INF .
	rm -rf src/main/webapp/*
	mv WEB-INF src/main/webapp/
	$(MAVEN) clean
