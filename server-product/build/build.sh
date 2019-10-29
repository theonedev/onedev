#!/bin/bash 
set -e
buildVersion=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout -f ../../pom.xml)
mvn clean package -f ../../pom.xml

mkdir ../target/docker
cp docker/* ../target/docker
unzip ../target/onedev-${buildVersion}.zip -d ../target/docker
mv ../target/docker/onedev-${buildVersion} ../target/docker/onedev
cp ../target/sandbox/site/lib/mysql* ../target/sandbox/site/lib/postgresql* ../target/docker/onedev/site/lib
docker build -t 1dev/server:${buildVersion} ../target/docker
docker push 1dev/server:${buildVersion}

cp -r k8s ../target/k8s
find ../target/k8s -name "*.yaml" | xargs sed -i -e "s/\${buildVersion}/${buildVersion}/g"
