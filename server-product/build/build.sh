#!/bin/sh
set -e
docker login -u robinshen -p $@
buildVersion=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout -f ../../pom.xml)
mvn clean package -f ../../pom.xml

mkdir ../target/docker
cp docker/* ../target/docker

cat << EOF > ../target/docker/run.sh
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v \$(which docker):/usr/bin/docker -v /opt/onedev:/opt/onedev -p 6610:6610 1dev/server:${buildVersion}
EOF
chmod +x ../target/docker/run.sh

unzip ../target/onedev-${buildVersion}.zip -d ../target/docker
mv ../target/docker/onedev-${buildVersion} ../target/docker/onedev
cp ../target/sandbox/site/lib/mysql* ../target/sandbox/site/lib/postgresql* ../target/docker/onedev/site/lib
docker build -t 1dev/server:${buildVersion} ../target/docker
docker push 1dev/server:${buildVersion}
docker tag 1dev/server:${buildVersion} 1dev/server:latest
docker push 1dev/server:latest

cp -r k8s ../target/k8s
find ../target/k8s -name "*.yaml" | xargs sed -i -e "s/\${buildVersion}/${buildVersion}/g"
