#!/bin/bash

set -e

cd ../target

buildVersion=`ls onedev-*.zip|sed -e 's/onedev-\(.*\).zip/\1/'`

rm -rf helm-chart/onedev/*
rm -rf helm-chart/*.tgz
mkdir -p helm-chart/onedev
cp -r ../helm/* helm-chart/onedev
rm helm-chart/onedev/build.sh

if [[ "$OSTYPE" == "darwin"* ]]; then
	find helm-chart -name "*.yaml" | xargs sed -i '' "s/\${buildVersion}/${buildVersion}/g"
else
	find helm-chart -name "*.yaml" | xargs sed -i -e "s/\${buildVersion}/${buildVersion}/g"
fi

cd helm-chart
tar zcvf onedev-${buildVersion}.tgz onedev
