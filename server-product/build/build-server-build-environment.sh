#!/bin/sh
set -e
version=1.0
docker build -t 1dev/server-build-environment:$version -f docker/Dockerfile.server-build-environment .
docker login -u robinshen -p $@
docker push 1dev/server-build-environment:$version
