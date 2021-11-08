#!/bin/bash

set -e

cd ../target

buildVersion=`ls onedev-*.zip|sed -e 's/onedev-\(.*\).zip/\1/'`

rm -rf helm-resources
cp -r ../helm helm-resources
rm helm-resources/build.sh

if [[ "$OSTYPE" == "darwin"* ]]; then
	find helm-resources -name "*.yaml" | xargs sed -i '' "s/\${buildVersion}/${buildVersion}/g"
else
	find helm-resources -name "*.yaml" | xargs sed -i -e "s/\${buildVersion}/${buildVersion}/g"
fi

zip -r helm-resources.zip helm-resources


