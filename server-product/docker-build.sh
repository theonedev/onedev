#!/bin/sh
unzip target/onedev-*.zip -d target/extracted
mv target/extracted/onedev-* target/extracted/onedev
docker build -t 1dev/server:latest . 
rm -rf target/extracted
