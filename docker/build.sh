#!/bin/bash

cd ../..

echo "Detecting project version (may require some time while downloading maven dependencies)..."
buildVersion=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout)

mvn clean package

cp -r server-product/docker docker
cp server-product/target/onedev-${buildVersion}.zip .
unzip onedev-${buildVersion}.zip -d docker
mv docker/onedev-${buildVersion} docker/app
cp server-product/target/sandbox/site/lib/mysql* server-product/target/sandbox/site/lib/postgresql* docker/app/site/lib

docker build -t 1dev/server:${buildVersion} docker
