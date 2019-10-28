#!/bin/sh
mvn clean package -f ../pom.xml
rm -rf build onedev-*
unzip ../server-product/target/onedev-*.zip 
mv onedev-* build
cp ../server-product/target/sandbox/site/lib/mysql* ../server-product/target/sandbox/site/lib/postgresql* build/site/lib
docker build -t 1dev/server:latest . 
docker push 1dev/server:latest
rm -rf build
