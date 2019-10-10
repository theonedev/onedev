#!/bin/sh
rm -rf build onedev-*
unzip ../target/onedev-*.zip 
mv onedev-* build
cp ../target/sandbox/site/lib/mysql* ../target/sandbox/site/lib/postgresql* build/site/lib
docker build -t 1dev/server:latest . 
docker push 1dev/server:latest
rm -rf build
