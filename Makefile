

MAVEN = mvn

default: server

localsvr: pom.xml
	echo "//TODO: Remove datastore specification"
	echo "//TODO: Checkout original version from git"

server: pom.xml
	$(MAVEN) package

clean: pom.xml
	$(MAVEN) clean
