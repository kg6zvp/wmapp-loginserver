

MAVEN = mvn

docsPath=src/main/webapp/docs

default: server

localsvr: pom.xml
	echo "//TODO: Remove datastore specification"
	echo "//TODO: Checkout original version from git"

server: pom.xml
	mkdir -p $(docsPath)
	apidoc -i ./ -o $(docsPath)/
	sed -i "/\/\/ Setup jQuery Ajax/a var tUrl = window.location.href;\n    apiProject.url = tUrl.substring(0, tUrl.substring(0, tUrl.length-3).lastIndexOf('\/'));\n    console.log(\"Url: \"+apiProject.url);" 'src/main/webapp/docs/main.js'
	$(MAVEN) package

clean: pom.xml
	rm -rf $(docsPath)
	$(MAVEN) clean


#mv src/main/webapp/WEB-INF .
#mv WEB-INF src/main/webapp/
