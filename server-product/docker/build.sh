#!/bin/bash

set -e

BUILDER="builder-$(date +%s)"

function finish {
  docker buildx stop $BUILDER
  docker buildx rm $BUILDER
}
trap finish EXIT

cd ../target
rm -rf docker
cp -r ../docker docker
buildVersion=`ls onedev-*.zip|sed -e 's/onedev-\(.*\).zip/\1/'`

unzip onedev-$buildVersion.zip -d docker
mv docker/onedev-$buildVersion docker/app
cp sandbox/site/lib/mysql* sandbox/site/lib/postgresql* docker/app/site/lib

docker buildx create --name $BUILDER
docker buildx use $BUILDER

cp -r agent docker/
if [ "$1" != "release" ] ; then
  docker buildx build --push --platform linux/amd64,linux/arm64 -f docker/Dockerfile.server -t 1dev/server:test docker
  docker buildx build --push --platform linux/amd64,linux/arm64 -f docker/Dockerfile.agent -t 1dev/agent:test docker
else
  docker buildx build --push --platform linux/amd64,linux/arm64 -f docker/Dockerfile.server -t 1dev/server:$buildVersion -t 1dev/server:latest docker
  docker buildx build --push --platform linux/amd64,linux/arm64 -f docker/Dockerfile.agent -t 1dev/agent:latest docker
fi
