#!/bin/bash

set -e

cd ../target

buildVersion=`ls onedev-*.zip|sed -e 's/onedev-\(.*\).zip/\1/'`

rm -rf k8s-resources
cp -r ../k8s k8s-resources
rm k8s-resources/build.sh
find k8s-resources -name "*.yaml" | xargs sed -i -e "s/\${buildVersion}/${buildVersion}/g"
zip -r k8s-resources.zip k8s-resources
